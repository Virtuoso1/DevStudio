import { Router } from "express";
import { authenticate, requireRole } from "../../middleware/auth";
import { asyncHandler } from "../../utils/asyncHandler";
import { getMessages, listConversations, sendMessage, startConversation } from "./chat.controller";

const router = Router();

router.use(authenticate);

router.post("/", requireRole("CUSTOMER"), asyncHandler(startConversation));
router.get("/", asyncHandler(listConversations));
router.get("/:id/messages", asyncHandler(getMessages));
router.post("/:id/messages", asyncHandler(sendMessage));

export default router;