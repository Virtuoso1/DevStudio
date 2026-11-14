import fs from "fs";
import dotenv from "dotenv";
import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    include: ["tests/**/*.test.ts"],
    env: { ...dotenv.parse(fs.readFileSync(".env.test")), NODE_ENV: "test" },
    fileParallelism: false,
    testTimeout: 20000,
  },
});