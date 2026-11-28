import { Request, Response } from "express";
import { z } from "zod";
import { prisma } from "../../lib/prisma";
import { AppError } from "../../utils/AppError";

const idParam = z.object({ id: z.string().uuid() });
const reviewSchema = z.object({
  rating: z.number().int().min(1).max(5),
  comment: z.string().trim().max(500).optional(),
});

export async function createReview(req: Request, res: Response) {
  const { id } = idParam.parse(req.params);
  const data = reviewSchema.parse(req.body);

  const booking = await prisma.booking.findUnique({
    where: { id },
    include: { review: true },
  });
  if (!booking) throw new AppError(404, "Booking not found");
  if (booking.customerId !== req.user!.id) throw new AppError(403, "This is not your booking");
  if (booking.status !== "COMPLETED") {
    throw new AppError(400, "You can only review a completed booking");
  }
  if (booking.review) throw new AppError(409, "You already reviewed this booking");

  const review = await prisma.$transaction(async (tx) => {
    // Keep concurrent reviews and the provider rating in the same update order.
    await tx.$queryRaw`SELECT "id" FROM "Provider" WHERE "id" = ${booking.providerId} FOR UPDATE`;
    if (await tx.review.findUnique({ where: { bookingId: id } })) {
      throw new AppError(409, "You already reviewed this booking");
    }
    const created = await tx.review.create({
      data: {
        bookingId: id,
        customerId: req.user!.id,
        providerId: booking.providerId,
        rating: data.rating,
        comment: data.comment,
      },
    });

    const agg = await tx.review.aggregate({
      where: { providerId: booking.providerId },
      _avg: { rating: true },
      _count: { rating: true },
    });

    await tx.provider.update({
      where: { id: booking.providerId },
      data: {
        ratingAvg: Math.round((agg._avg.rating ?? 0) * 10) / 10,
        ratingCount: agg._count.rating,
      },
    });

    return created;
  });

  res.status(201).json(review);
}