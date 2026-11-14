import "dotenv/config";
import express from "express";
import cors from "cors";
import helmet from "helmet";
import rateLimit from "express-rate-limit";
import swaggerUi from "swagger-ui-express";
import { prisma } from "./lib/prisma";
import { openapi } from "./docs/openapi";
import { errorHandler, notFound } from "./middleware/errorHandler";
import { authenticate } from "./middleware/auth";
import { asyncHandler } from "./utils/asyncHandler";
import authRoutes from "./modules/auth/auth.routes";
import { getMe } from "./modules/auth/auth.controller";
import providerRoutes from "./modules/providers/providers.routes";
import bookingRoutes from "./modules/bookings/bookings.routes";
import chatRoutes from "./modules/chat/chat.routes";

export const app = express();

const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  limit: 30,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: "Too many attempts, please try again later" },
  skip: () => process.env.NODE_ENV === "test",
});

app.use(helmet());
app.use(cors());
app.use(express.json());

app.get("/health", async (_req, res) => {
  try {
    await prisma.$queryRaw`SELECT 1`;
    res.json({ status: "ok", database: "connected" });
  } catch {
    res.status(500).json({ status: "error", database: "unreachable" });
  }
});

app.use("/docs", swaggerUi.serve, swaggerUi.setup(openapi));
app.use("/auth", authLimiter, authRoutes);
app.get("/me", authenticate, asyncHandler(getMe));
app.use("/providers", providerRoutes);
app.use("/bookings", bookingRoutes);
app.use("/conversations", chatRoutes);

app.use(notFound);
app.use(errorHandler);