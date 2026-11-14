import bcrypt from "bcrypt";
import jwt from "jsonwebtoken";
import { Role } from "@prisma/client";
import { Request, Response } from "express";
import { prisma } from "../../lib/prisma";
import { AppError } from "../../utils/AppError";
import { loginSchema, registerSchema } from "./auth.schemas";

function signToken(user: { id: string; role: Role }) {
  return jwt.sign({ sub: user.id, role: user.role }, process.env.JWT_SECRET as string, {
    expiresIn: (process.env.JWT_EXPIRES_IN || "7d") as jwt.SignOptions["expiresIn"],
  });
}

function publicUser<T extends { passwordHash: string }>(user: T) {
  const { passwordHash, ...rest } = user;
  return rest;
}

export async function register(req: Request, res: Response) {
  const data = registerSchema.parse(req.body);

  const exists = await prisma.user.findUnique({ where: { email: data.email } });
  if (exists) throw new AppError(409, "Email already registered");

  const passwordHash = await bcrypt.hash(data.password, 10);

  const user = await prisma.user.create({
    data: {
      name: data.name,
      email: data.email,
      phone: data.phone,
      passwordHash,
      role: data.role,
      ...(data.role === "PROVIDER"
        ? {
            provider: {
              create: {
                trade: data.trade!,
                bio: data.bio!,
                yearsExperience: data.yearsExperience!,
                credentials: data.credentials,
              },
            },
          }
        : {}),
    },
    include: { provider: true },
  });

  res.status(201).json({ token: signToken(user), user: publicUser(user) });
}

export async function login(req: Request, res: Response) {
  const data = loginSchema.parse(req.body);

  const user = await prisma.user.findUnique({
    where: { email: data.email },
    include: { provider: true },
  });
  if (!user || !(await bcrypt.compare(data.password, user.passwordHash))) {
    throw new AppError(401, "Invalid email or password");
  }

  res.json({ token: signToken(user), user: publicUser(user) });
}

export async function getMe(req: Request, res: Response) {
  const user = await prisma.user.findUnique({
    where: { id: req.user!.id },
    include: { provider: true },
  });
  if (!user) throw new AppError(404, "User not found");
  res.json({ user: publicUser(user) });
}