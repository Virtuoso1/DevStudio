const bearer = [{ bearerAuth: [] }];
const json = (schema: object) => ({ required: true, content: { "application/json": { schema } } });
const id = { name: "id", in: "path", required: true, schema: { type: "string", format: "uuid" } };
const str = { type: "string" };

export const openapi: Record<string, any> = {
  openapi: "3.0.3",
  info: {
    title: "Home Services API",
    version: "1.0.0",
    description: "Backend for the Home Services Android app: find, compare and book local tradespeople.",
  },
  servers: [{ url: "http://localhost:3000" }],
  components: {
    securitySchemes: { bearerAuth: { type: "http", scheme: "bearer", bearerFormat: "JWT" } },
  },
  paths: {
    "/auth/register": {
      post: {
        tags: ["Auth"],
        summary: "Register a customer or provider",
        requestBody: json({
          type: "object",
          required: ["name", "email", "password"],
          properties: {
            name: str, email: str, phone: str, password: { type: "string", minLength: 8 },
            role: { type: "string", enum: ["CUSTOMER", "PROVIDER"] },
            trade: { type: "string", enum: ["ELECTRICIAN", "PLUMBER", "APPLIANCE"] },
            bio: str, yearsExperience: { type: "integer" }, credentials: str,
          },
        }),
        responses: { 201: { description: "Created, returns token and user" }, 400: { description: "Validation failed" }, 409: { description: "Email already registered" } },
      },
    },
    "/auth/login": {
      post: {
        tags: ["Auth"],
        summary: "Log in",
        requestBody: json({ type: "object", required: ["email", "password"], properties: { email: str, password: str } }),
        responses: { 200: { description: "Returns token and user" }, 401: { description: "Invalid email or password" } },
      },
    },
    "/me": {
      get: { tags: ["Auth"], summary: "Current user", security: bearer, responses: { 200: { description: "User" }, 401: { description: "Unauthorized" } } },
    },
    "/providers": {
      get: {
        tags: ["Providers"],
        summary: "List and search providers",
        parameters: [
          { name: "trade", in: "query", schema: { type: "string", enum: ["ELECTRICIAN", "PLUMBER", "APPLIANCE"] } },
          { name: "q", in: "query", schema: str, description: "Search by name or bio" },
          { name: "page", in: "query", schema: { type: "integer", default: 1 } },
          { name: "limit", in: "query", schema: { type: "integer", default: 20, maximum: 50 } },
        ],
        responses: { 200: { description: "Paged list of providers" } },
      },
    },
    "/providers/{id}": {
      get: { tags: ["Providers"], summary: "Provider profile", parameters: [id], responses: { 200: { description: "Provider" }, 404: { description: "Not found" } } },
    },
    "/providers/{id}/reviews": {
      get: { tags: ["Providers"], summary: "Provider reviews", parameters: [id], responses: { 200: { description: "Reviews, newest first" }, 404: { description: "Not found" } } },
    },
    "/bookings": {
      post: {
        tags: ["Bookings"],
        summary: "Create a booking (customers only)",
        security: bearer,
        requestBody: json({
          type: "object",
          required: ["providerId", "date", "timeSlot", "issueDescription"],
          properties: { providerId: { type: "string", format: "uuid" }, date: { type: "string", example: "2026-12-01" }, timeSlot: { type: "string", example: "09:00-11:00" }, issueDescription: str },
        }),
        responses: { 201: { description: "Booking created as PENDING" }, 409: { description: "Time slot already booked" } },
      },
      get: {
        tags: ["Bookings"],
        summary: "My bookings (customer's own, or provider's received)",
        security: bearer,
        parameters: [{ name: "status", in: "query", schema: { type: "string", enum: ["PENDING", "CONFIRMED", "COMPLETED", "CANCELLED"] } }],
        responses: { 200: { description: "List of bookings" } },
      },
    },
    "/bookings/book-again": {
      get: { tags: ["Bookings"], summary: "Providers the customer has completed jobs with", security: bearer, responses: { 200: { description: "Providers" } } },
    },
    "/bookings/{id}/status": {
      patch: {
        tags: ["Bookings"],
        summary: "Change booking status (providers confirm or complete, customers cancel)",
        security: bearer,
        parameters: [id],
        requestBody: json({ type: "object", required: ["status"], properties: { status: { type: "string", enum: ["CONFIRMED", "COMPLETED", "CANCELLED"] } } }),
        responses: { 200: { description: "Updated booking" }, 400: { description: "Invalid transition" }, 403: { description: "Not allowed" } },
      },
    },
    "/bookings/{id}/reviews": {
      post: {
        tags: ["Reviews"],
        summary: "Review a completed booking (customer only)",
        security: bearer,
        parameters: [id],
        requestBody: json({ type: "object", required: ["rating"], properties: { rating: { type: "integer", minimum: 1, maximum: 5 }, comment: str } }),
        responses: { 201: { description: "Review created" }, 400: { description: "Booking not completed" }, 409: { description: "Already reviewed" } },
      },
    },
    "/conversations": {
      post: {
        tags: ["Chat"],
        summary: "Start or reopen a conversation with a provider (customers only)",
        security: bearer,
        requestBody: json({ type: "object", required: ["providerId"], properties: { providerId: { type: "string", format: "uuid" } } }),
        responses: { 201: { description: "Conversation id" } },
      },
      get: { tags: ["Chat"], summary: "My conversations with the last message", security: bearer, responses: { 200: { description: "Conversations" } } },
    },
    "/conversations/{id}/messages": {
      get: {
        tags: ["Chat"],
        summary: "Messages in a conversation",
        security: bearer,
        parameters: [id, { name: "since", in: "query", schema: { type: "string", format: "date-time" }, description: "Only messages after this time (for polling)" }],
        responses: { 200: { description: "Messages, oldest first" }, 403: { description: "Not a participant" } },
      },
      post: {
        tags: ["Chat"],
        summary: "Send a message",
        security: bearer,
        parameters: [id],
        requestBody: json({ type: "object", required: ["body"], properties: { body: str } }),
        responses: { 201: { description: "Message sent" } },
      },
    },
  },
};
