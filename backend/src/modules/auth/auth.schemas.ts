import { z } from "zod";

export const registerSchema = z
  .object({
    name: z.string().trim().min(2),
    email: z.string().trim().toLowerCase().email(),
    phone: z.string().trim().optional(),
    password: z.string().min(8, "Password must be at least 8 characters"),
    role: z.enum(["CUSTOMER", "PROVIDER"]).default("CUSTOMER"),
    trade: z.enum(["ELECTRICIAN", "PLUMBER", "APPLIANCE"]).optional(),
    bio: z.string().trim().min(10).optional(),
    yearsExperience: z.number().int().min(0).optional(),
    credentials: z.string().trim().optional(),
  })
  .superRefine((d, ctx) => {
    if (d.role === "PROVIDER") {
      for (const f of ["trade", "bio", "yearsExperience"] as const) {
        if (d[f] === undefined) {
          ctx.addIssue({ code: "custom", path: [f], message: `${f} is required for providers` });
        }
      }
    }
  });

export const loginSchema = z.object({
  email: z.string().trim().toLowerCase().email(),
  password: z.string().min(1),
});