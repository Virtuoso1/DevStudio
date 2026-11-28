import { BookingStatus, Prisma } from "@prisma/client";
import { Request, Response } from "express";
import { z } from "zod";
import { prisma } from "../../lib/prisma";
import { AppError } from "../../utils/AppError";

const createSchema = z.object({
  providerId: z.string().uuid(),
  date: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Date must be YYYY-MM-DD"),
  timeSlot: z.string().trim().min(3),
  issueDescription: z.string().trim().min(5),
});
const idParam = z.object({ id: z.string().uuid() });
const statusSchema = z.object({ status: z.enum(["CONFIRMED", "COMPLETED", "CANCELLED"]) });
const listQuery = z.object({
  status: z.enum(["PENDING", "CONFIRMED", "COMPLETED", "CANCELLED"]).optional(),
});

const bookingSelect = {
  id: true,
  date: true,
  timeSlot: true,
  issueDescription: true,
  status: true,
  createdAt: true,
  review: { select: { id: true } },
  provider: { select: { id: true, trade: true, user: { select: { name: true } } } },
  customer: { select: { id: true, name: true } },
} satisfies Prisma.BookingSelect;

type BookingRow = Prisma.BookingGetPayload<{ select: typeof bookingSelect }>;

function toDto(b: BookingRow) {
  return {
    id: b.id,
    date: b.date.toISOString().slice(0, 10),
    timeSlot: b.timeSlot,
    issueDescription: b.issueDescription,
    status: b.status,
    createdAt: b.createdAt,
    review: b.review,
    provider: { id: b.provider.id, name: b.provider.user.name, trade: b.provider.trade },
    customer: b.customer,
  };
}

export async function createBooking(req: Request, res: Response) {
  const data = createSchema.parse(req.body);

  const date = new Date(`${data.date}T00:00:00.000Z`);
  if (isNaN(date.getTime()) || date.toISOString().slice(0, 10) !== data.date) {
    throw new AppError(400, "Invalid date");
  }
  const today = new Date();
  today.setUTCHours(0, 0, 0, 0);
  if (date < today) throw new AppError(400, "Date cannot be in the past");

  const booking = await prisma.$transaction(async (tx) => {
    // Serialise slot checks for this provider until the booking is committed.
    const [provider] = await tx.$queryRaw<{ userId: string }[]>`
      SELECT "userId" FROM "Provider" WHERE "id" = ${data.providerId} FOR UPDATE
    `;
    if (!provider) throw new AppError(404, "Provider not found");
    if (provider.userId === req.user!.id) throw new AppError(400, "You cannot book yourself");

    const clash = await tx.booking.findFirst({
      where: {
        providerId: data.providerId,
        date,
        timeSlot: data.timeSlot,
        status: { in: ["PENDING", "CONFIRMED"] },
      },
    });
    if (clash) throw new AppError(409, "That time slot is already booked");

    return tx.booking.create({
      data: {
        customerId: req.user!.id,
        providerId: data.providerId,
        date,
        timeSlot: data.timeSlot,
        issueDescription: data.issueDescription,
      },
      select: bookingSelect,
    });
  });

  res.status(201).json(toDto(booking));
}

export async function listBookings(req: Request, res: Response) {
  const { status } = listQuery.parse(req.query);

  let where: Prisma.BookingWhereInput;
  if (req.user!.role === "PROVIDER") {
    const provider = await prisma.provider.findUnique({ where: { userId: req.user!.id } });
    if (!provider) throw new AppError(404, "Provider profile not found");
    where = { providerId: provider.id };
  } else {
    where = { customerId: req.user!.id };
  }
  if (status) where = { ...where, status };

  const bookings = await prisma.booking.findMany({
    where,
    select: bookingSelect,
    orderBy: { date: "desc" },
  });
  res.json(bookings.map(toDto));
}

const allowedTransitions: Record<BookingStatus, BookingStatus[]> = {
  PENDING: ["CONFIRMED", "CANCELLED"],
  CONFIRMED: ["COMPLETED", "CANCELLED"],
  COMPLETED: [],
  CANCELLED: [],
};

export async function updateBookingStatus(req: Request, res: Response) {
  const { id } = idParam.parse(req.params);
  const { status } = statusSchema.parse(req.body);

  const booking = await prisma.booking.findUnique({
    where: { id },
    include: { provider: { select: { userId: true } } },
  });
  if (!booking) throw new AppError(404, "Booking not found");

  const isCustomer = booking.customerId === req.user!.id;
  const isProvider = booking.provider.userId === req.user!.id;
  if (!isCustomer && !isProvider) throw new AppError(403, "This is not your booking");
  if (isCustomer && status !== "CANCELLED") {
    throw new AppError(403, "Customers can only cancel bookings");
  }
  if (!allowedTransitions[booking.status].includes(status)) {
    throw new AppError(400, `Cannot change status from ${booking.status} to ${status}`);
  }

  const updated = await prisma.booking.updateMany({
    where: { id, status: booking.status },
    data: { status },
  });
  if (!updated.count) throw new AppError(409, "Booking status changed. Refresh and try again.");
  res.json(toDto(await prisma.booking.findUniqueOrThrow({ where: { id }, select: bookingSelect })));
}

export async function bookAgain(req: Request, res: Response) {
  const rows = await prisma.booking.findMany({
    where: { customerId: req.user!.id, status: "COMPLETED" },
    distinct: ["providerId"],
    orderBy: { date: "desc" },
    take: 10,
    select: {
      provider: {
        select: {
          id: true,
          trade: true,
          bio: true,
          yearsExperience: true,
          ratingAvg: true,
          ratingCount: true,
          user: { select: { name: true } },
        },
      },
    },
  });

  res.json(
    rows.map(({ provider: { user, ...p } }) => ({ ...p, name: user.name }))
  );
}