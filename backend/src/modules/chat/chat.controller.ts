import { Request, Response } from "express";
import { z } from "zod";
import { prisma } from "../../lib/prisma";
import { AppError } from "../../utils/AppError";

const idParam = z.object({ id: z.string().uuid() });
const startSchema = z.object({ providerId: z.string().uuid() });
const sendSchema = z.object({ body: z.string().trim().min(1).max(1000) });
const messagesQuery = z.object({ since: z.coerce.date().optional() });

async function loadForParticipant(userId: string, conversationId: string) {
  const conversation = await prisma.conversation.findUnique({
    where: { id: conversationId },
    include: { provider: { select: { userId: true } } },
  });
  if (!conversation) throw new AppError(404, "Conversation not found");
  if (conversation.customerId !== userId && conversation.provider.userId !== userId) {
    throw new AppError(403, "You are not part of this conversation");
  }
  return conversation;
}

export async function startConversation(req: Request, res: Response) {
  const { providerId } = startSchema.parse(req.body);

  const provider = await prisma.provider.findUnique({ where: { id: providerId } });
  if (!provider) throw new AppError(404, "Provider not found");
  if (provider.userId === req.user!.id) throw new AppError(400, "You cannot message yourself");

  const conversation = await prisma.conversation.upsert({
    where: { customerId_providerId: { customerId: req.user!.id, providerId } },
    update: {},
    create: { customerId: req.user!.id, providerId },
  });

  res.status(201).json({ id: conversation.id, providerId });
}

export async function listConversations(req: Request, res: Response) {
  let where;
  if (req.user!.role === "PROVIDER") {
    const provider = await prisma.provider.findUnique({ where: { userId: req.user!.id } });
    if (!provider) throw new AppError(404, "Provider profile not found");
    where = { providerId: provider.id };
  } else {
    where = { customerId: req.user!.id };
  }

  const conversations = await prisma.conversation.findMany({
    where,
    include: {
      customer: { select: { id: true, name: true } },
      provider: { select: { id: true, trade: true, user: { select: { name: true } } } },
      messages: { orderBy: { createdAt: "desc" }, take: 1 },
    },
  });

  const items = conversations
    .map((c) => {
      const last = c.messages[0];
      return {
        id: c.id,
        otherParty:
          req.user!.role === "PROVIDER"
            ? { name: c.customer.name }
            : { name: c.provider.user.name, trade: c.provider.trade },
        lastMessage: last
          ? { id: last.id, body: last.body, senderId: last.senderId, createdAt: last.createdAt }
          : null,
        sortKey: (last?.createdAt ?? c.createdAt).getTime(),
      };
    })
    .sort((a, b) => b.sortKey - a.sortKey)
    .map(({ sortKey, ...rest }) => rest);

  res.json(items);
}

export async function getMessages(req: Request, res: Response) {
  const { id } = idParam.parse(req.params);
  const { since } = messagesQuery.parse(req.query);
  await loadForParticipant(req.user!.id, id);

  const messages = await prisma.message.findMany({
    where: { conversationId: id, ...(since ? { createdAt: { gt: since } } : {}) },
    orderBy: { createdAt: "asc" },
    take: 200,
    select: { id: true, body: true, senderId: true, createdAt: true },
  });
  res.json(messages);
}

export async function sendMessage(req: Request, res: Response) {
  const { id } = idParam.parse(req.params);
  const { body } = sendSchema.parse(req.body);
  await loadForParticipant(req.user!.id, id);

  const message = await prisma.message.create({
    data: { conversationId: id, senderId: req.user!.id, body },
    select: { id: true, body: true, senderId: true, createdAt: true },
  });
  res.status(201).json(message);
}