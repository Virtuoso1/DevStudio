import { Request, Response } from "express";
import { z } from "zod";
import { prisma } from "../../lib/prisma";
import { AppError } from "../../utils/AppError";

const listQuery = z.object({
  trade: z.enum(["ELECTRICIAN", "PLUMBER", "APPLIANCE"]).optional(),
  q: z.string().trim().optional(),
  page: z.coerce.number().int().min(1).default(1),
  limit: z.coerce.number().int().min(1).max(50).default(20),
});

const idParam = z.object({ id: z.string().uuid() });

const providerSelect = {
  id: true,
  trade: true,
  bio: true,
  yearsExperience: true,
  credentials: true,
  ratingAvg: true,
  ratingCount: true,
  user: { select: { name: true } },
} as const;

function toDto(p: {
  id: string;
  trade: string;
  bio: string;
  yearsExperience: number;
  credentials: string | null;
  ratingAvg: number;
  ratingCount: number;
  user: { name: string };
}) {
  const { user, ...rest } = p;
  return { ...rest, name: user.name };
}

export async function listProviders(req: Request, res: Response) {
  const { trade, q, page, limit } = listQuery.parse(req.query);

  const where = {
    ...(trade ? { trade } : {}),
    ...(q
      ? {
          OR: [
            { user: { name: { contains: q, mode: "insensitive" as const } } },
            { bio: { contains: q, mode: "insensitive" as const } },
          ],
        }
      : {}),
  };

  const [total, items] = await Promise.all([
    prisma.provider.count({ where }),
    prisma.provider.findMany({
      where,
      select: providerSelect,
      orderBy: [{ ratingAvg: "desc" }, { ratingCount: "desc" }],
      skip: (page - 1) * limit,
      take: limit,
    }),
  ]);

  res.json({ page, limit, total, items: items.map(toDto) });
}

export async function getProvider(req: Request, res: Response) {
  const { id } = idParam.parse(req.params);
  const provider = await prisma.provider.findUnique({ where: { id }, select: providerSelect });
  if (!provider) throw new AppError(404, "Provider not found");
  res.json(toDto(provider));
}

export async function getProviderReviews(req: Request, res: Response) {
  const { id } = idParam.parse(req.params);
  const exists = await prisma.provider.findUnique({ where: { id }, select: { id: true } });
  if (!exists) throw new AppError(404, "Provider not found");

  const reviews = await prisma.review.findMany({
    where: { providerId: id },
    orderBy: { createdAt: "desc" },
    select: {
      id: true,
      rating: true,
      comment: true,
      createdAt: true,
      customer: { select: { name: true } },
    },
  });

  res.json(
    reviews.map(({ customer, ...r }) => ({ ...r, customerName: customer.name }))
  );
}