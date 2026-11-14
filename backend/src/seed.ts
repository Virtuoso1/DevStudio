import "dotenv/config";
import bcrypt from "bcrypt";
import { Trade } from "@prisma/client";
import { prisma } from "./lib/prisma";

const providers: {
  name: string;
  email: string;
  trade: Trade;
  years: number;
  bio: string;
  credentials: string;
  ratingAvg: number;
  ratingCount: number;
}[] = [
  { name: "Grace Wanjiku", email: "grace@homeservices.test", trade: "PLUMBER", years: 6, bio: "Leak repairs, pipe installation and bathroom fittings.", credentials: "Certified plumber, NITA Grade 2", ratingAvg: 4.8, ratingCount: 24 },
  { name: "Peter Otieno", email: "peter@homeservices.test", trade: "ELECTRICIAN", years: 12, bio: "House wiring, fault finding and solar installations.", credentials: "EPRA licensed electrician", ratingAvg: 4.9, ratingCount: 41 },
  { name: "Amina Hassan", email: "amina@homeservices.test", trade: "APPLIANCE", years: 5, bio: "Fridge, washing machine and oven repairs at your home.", credentials: "Samsung and LG trained technician", ratingAvg: 4.6, ratingCount: 18 },
  { name: "Brian Kiptoo", email: "brian@homeservices.test", trade: "PLUMBER", years: 9, bio: "Water heaters, drainage and emergency plumbing.", credentials: "Certified plumber, NITA Grade 1", ratingAvg: 4.7, ratingCount: 33 },
  { name: "Lucy Njeri", email: "lucy@homeservices.test", trade: "ELECTRICIAN", years: 4, bio: "Lighting, sockets and security light installation.", credentials: "EPRA licensed electrician", ratingAvg: 4.5, ratingCount: 12 },
  { name: "Samuel Mutua", email: "samuel@homeservices.test", trade: "APPLIANCE", years: 10, bio: "Microwaves, cookers, TVs and small appliance repair.", credentials: "Certified electronics technician", ratingAvg: 4.8, ratingCount: 29 },
];

async function main() {
  const passwordHash = await bcrypt.hash("Password123", 10);

  for (const p of providers) {
    await prisma.user.upsert({
      where: { email: p.email },
      update: {},
      create: {
        name: p.name,
        email: p.email,
        passwordHash,
        role: "PROVIDER",
        provider: {
          create: {
            trade: p.trade,
            bio: p.bio,
            yearsExperience: p.years,
            credentials: p.credentials,
            ratingAvg: p.ratingAvg,
            ratingCount: p.ratingCount,
          },
        },
      },
    });
  }

  await prisma.user.upsert({
    where: { email: "demo@homeservices.test" },
    update: {},
    create: { name: "Demo Customer", email: "demo@homeservices.test", passwordHash, role: "CUSTOMER" },
  });

  console.log("Seed complete. All seeded accounts use the password: Password123");
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());