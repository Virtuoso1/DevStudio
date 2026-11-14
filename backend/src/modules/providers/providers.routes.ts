import { Router } from "express";
import { asyncHandler } from "../../utils/asyncHandler";
import { getProvider, getProviderReviews, listProviders } from "./providers.controller";

const router = Router();

router.get("/", asyncHandler(listProviders));
router.get("/:id/reviews", asyncHandler(getProviderReviews));
router.get("/:id", asyncHandler(getProvider));

export default router;