import { Router } from "express";
import { authenticate, requireRole } from "../../middleware/auth";
import { asyncHandler } from "../../utils/asyncHandler";
import { bookAgain, createBooking, listBookings, updateBookingStatus } from "./bookings.controller";
import { createReview } from "../reviews/reviews.controller";

const router = Router();

router.use(authenticate);

router.post("/", requireRole("CUSTOMER"), asyncHandler(createBooking));
router.get("/", asyncHandler(listBookings));
router.get("/book-again", requireRole("CUSTOMER"), asyncHandler(bookAgain));
router.patch("/:id/status", asyncHandler(updateBookingStatus));
router.post("/:id/reviews", requireRole("CUSTOMER"), asyncHandler(createReview));

export default router;