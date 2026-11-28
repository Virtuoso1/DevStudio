import { afterAll, beforeAll, describe, expect, it } from "vitest";
import request from "supertest";
import { app } from "../src/app";
import { prisma } from "../src/lib/prisma";

const futureDate = (days: number) =>
  new Date(Date.now() + days * 86400000).toISOString().slice(0, 10);
const auth = (token: string) => ({ Authorization: `Bearer ${token}` });

let customerToken = "";
let otherToken = "";
let providerToken = "";
let providerId = "";
let bookingId = "";
let conversationId = "";

beforeAll(async () => {
  // Safety guard: these tests wipe the database, so they must never touch real data.
  const database = process.env.DATABASE_URL;
  if (!database || new URL(database).pathname !== "/homeservices_test") {
    throw new Error("Refusing to run: DATABASE_URL is not the test database");
  }
  await prisma.message.deleteMany();
  await prisma.conversation.deleteMany();
  await prisma.review.deleteMany();
  await prisma.booking.deleteMany();
  await prisma.provider.deleteMany();
  await prisma.user.deleteMany();
});

afterAll(async () => {
  await prisma.$disconnect();
});

describe("health", () => {
  it("reports the database as connected", async () => {
    const res = await request(app).get("/health");
    expect(res.status).toBe(200);
    expect(res.body.database).toBe("connected");
  });
});

describe("auth", () => {
  it("registers a customer without exposing the password hash", async () => {
    const res = await request(app)
      .post("/auth/register")
      .send({ name: "Test Customer", email: "customer@test.com", password: "Password123" });
    expect(res.status).toBe(201);
    expect(res.body.token).toBeTruthy();
    expect(res.body.user.passwordHash).toBeUndefined();
    customerToken = res.body.token;
  });

  it("rejects a duplicate email", async () => {
    const res = await request(app)
      .post("/auth/register")
      .send({ name: "Test Customer", email: "customer@test.com", password: "Password123" });
    expect(res.status).toBe(409);
  });

  it("rejects a short password", async () => {
    const res = await request(app)
      .post("/auth/register")
      .send({ name: "Short Pass", email: "short@test.com", password: "123" });
    expect(res.status).toBe(400);
  });

  it("requires trade details for providers", async () => {
    const res = await request(app)
      .post("/auth/register")
      .send({ name: "No Trade", email: "notrade@test.com", password: "Password123", role: "PROVIDER" });
    expect(res.status).toBe(400);
  });

  it("registers a provider", async () => {
    const res = await request(app).post("/auth/register").send({
      name: "Test Electrician",
      email: "provider@test.com",
      password: "Password123",
      role: "PROVIDER",
      trade: "ELECTRICIAN",
      bio: "Licensed electrician for testing.",
      yearsExperience: 5,
    });
    expect(res.status).toBe(201);
    providerToken = res.body.token;
    providerId = res.body.user.provider.id;
    expect(providerId).toBeTruthy();
  });

  it("registers a second customer used as an outsider", async () => {
    const res = await request(app)
      .post("/auth/register")
      .send({ name: "Outsider", email: "outsider@test.com", password: "Password123" });
    expect(res.status).toBe(201);
    otherToken = res.body.token;
  });

  it("logs in with correct credentials", async () => {
    const res = await request(app)
      .post("/auth/login")
      .send({ email: "customer@test.com", password: "Password123" });
    expect(res.status).toBe(200);
    expect(res.body.token).toBeTruthy();
  });

  it("rejects a wrong password", async () => {
    const res = await request(app)
      .post("/auth/login")
      .send({ email: "customer@test.com", password: "wrong-password" });
    expect(res.status).toBe(401);
  });

  it("blocks /me without a token", async () => {
    const res = await request(app).get("/me");
    expect(res.status).toBe(401);
  });

  it("returns the current user on /me", async () => {
    const res = await request(app).get("/me").set(auth(customerToken));
    expect(res.status).toBe(200);
    expect(res.body.user.email).toBe("customer@test.com");
  });
});

describe("providers", () => {
  it("lists providers filtered by trade", async () => {
    const res = await request(app).get("/providers").query({ trade: "ELECTRICIAN" });
    expect(res.status).toBe(200);
    expect(res.body.items.length).toBe(1);
    expect(res.body.items[0].name).toBe("Test Electrician");
  });

  it("returns nothing for a trade with no providers", async () => {
    const res = await request(app).get("/providers").query({ trade: "PLUMBER" });
    expect(res.body.items.length).toBe(0);
  });

  it("returns 404 for an unknown provider", async () => {
    const res = await request(app).get("/providers/00000000-0000-4000-8000-000000000000");
    expect(res.status).toBe(404);
  });
});

describe("bookings and reviews", () => {
  const payload = () => ({
    providerId,
    date: futureDate(7),
    timeSlot: "09:00-11:00",
    issueDescription: "Socket sparks when I plug in",
  });

  it("stops providers from booking", async () => {
    const res = await request(app).post("/bookings").set(auth(providerToken)).send(payload());
    expect(res.status).toBe(403);
  });

  it("rejects a date in the past", async () => {
    const res = await request(app)
      .post("/bookings")
      .set(auth(customerToken))
      .send({ ...payload(), date: "2020-01-01" });
    expect(res.status).toBe(400);
  });

  it("allows only one booking when two customers request the same slot concurrently", async () => {
    const data = { ...payload(), date: futureDate(9) };
    const results = await Promise.all(
      [customerToken, otherToken].map((token) => request(app).post("/bookings").set(auth(token)).send(data)),
    );
    expect(results.map((result) => result.status).sort()).toEqual([201, 409]);
    await prisma.booking.delete({ where: { id: results.find((result) => result.status === 201)!.body.id } });
  });

  it("creates a booking as PENDING", async () => {
    const res = await request(app).post("/bookings").set(auth(customerToken)).send(payload());
    expect(res.status).toBe(201);
    expect(res.body.status).toBe("PENDING");
    bookingId = res.body.id;
  });

  it("prevents double booking the same slot", async () => {
    const res = await request(app).post("/bookings").set(auth(otherToken)).send(payload());
    expect(res.status).toBe(409);
  });

  it("stops customers from confirming", async () => {
    const res = await request(app)
      .patch(`/bookings/${bookingId}/status`)
      .set(auth(customerToken))
      .send({ status: "CONFIRMED" });
    expect(res.status).toBe(403);
  });

  it("stops strangers from changing the booking", async () => {
    const res = await request(app)
      .patch(`/bookings/${bookingId}/status`)
      .set(auth(otherToken))
      .send({ status: "CANCELLED" });
    expect(res.status).toBe(403);
  });

  it("blocks reviews before the job is completed", async () => {
    const res = await request(app)
      .post(`/bookings/${bookingId}/reviews`)
      .set(auth(customerToken))
      .send({ rating: 4 });
    expect(res.status).toBe(400);
  });

  it("lets the provider confirm then complete", async () => {
    const confirmed = await request(app)
      .patch(`/bookings/${bookingId}/status`)
      .set(auth(providerToken))
      .send({ status: "CONFIRMED" });
    expect(confirmed.status).toBe(200);
    expect(confirmed.body.status).toBe("CONFIRMED");

    const completed = await request(app)
      .patch(`/bookings/${bookingId}/status`)
      .set(auth(providerToken))
      .send({ status: "COMPLETED" });
    expect(completed.status).toBe(200);
    expect(completed.body.status).toBe("COMPLETED");
  });

  it("blocks cancelling a completed booking", async () => {
    const res = await request(app)
      .patch(`/bookings/${bookingId}/status`)
      .set(auth(providerToken))
      .send({ status: "CANCELLED" });
    expect(res.status).toBe(400);
  });

  it("allows a review on a completed booking", async () => {
    const res = await request(app)
      .post(`/bookings/${bookingId}/reviews`)
      .set(auth(customerToken))
      .send({ rating: 4, comment: "Good work" });
    expect(res.status).toBe(201);
  });

  it("rejects a second review for the same booking", async () => {
    const res = await request(app)
      .post(`/bookings/${bookingId}/reviews`)
      .set(auth(customerToken))
      .send({ rating: 5 });
    expect(res.status).toBe(409);
  });

  it("updates the provider rating", async () => {
    const res = await request(app).get(`/providers/${providerId}`);
    expect(res.body.ratingAvg).toBe(4);
    expect(res.body.ratingCount).toBe(1);
  });

  it("lists each side's own bookings", async () => {
    const customer = await request(app).get("/bookings").set(auth(customerToken));
    const provider = await request(app).get("/bookings").set(auth(providerToken));
    const outsider = await request(app).get("/bookings").set(auth(otherToken));
    expect(customer.body.length).toBe(1);
    expect(provider.body.length).toBe(1);
    expect(outsider.body.length).toBe(0);
  });
});

describe("chat", () => {
  it("starts a conversation", async () => {
    const res = await request(app)
      .post("/conversations")
      .set(auth(customerToken))
      .send({ providerId });
    expect(res.status).toBe(201);
    conversationId = res.body.id;
  });

  it("sends and receives messages in order", async () => {
    await request(app)
      .post(`/conversations/${conversationId}/messages`)
      .set(auth(customerToken))
      .send({ body: "Are you free on Monday?" });
    await request(app)
      .post(`/conversations/${conversationId}/messages`)
      .set(auth(providerToken))
      .send({ body: "Yes, 9am works." });

    const res = await request(app)
      .get(`/conversations/${conversationId}/messages`)
      .set(auth(customerToken));
    expect(res.status).toBe(200);
    expect(res.body.map((m: { body: string }) => m.body)).toEqual([
      "Are you free on Monday?",
      "Yes, 9am works.",
    ]);
  });

  it("rejects empty messages", async () => {
    const res = await request(app)
      .post(`/conversations/${conversationId}/messages`)
      .set(auth(customerToken))
      .send({ body: "   " });
    expect(res.status).toBe(400);
  });

  it("blocks outsiders from reading or writing", async () => {
    const read = await request(app)
      .get(`/conversations/${conversationId}/messages`)
      .set(auth(otherToken));
    const write = await request(app)
      .post(`/conversations/${conversationId}/messages`)
      .set(auth(otherToken))
      .send({ body: "hello" });
    expect(read.status).toBe(403);
    expect(write.status).toBe(403);
  });
});