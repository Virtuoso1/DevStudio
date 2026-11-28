# Home Services

Android app for finding and booking electricians, plumbers, and appliance technicians. The app uses Kotlin and Jetpack Compose. The REST API uses Express, Prisma, and PostgreSQL.

## Get the project and run locally

Already cloned? Run `git switch main` and `git pull --ff-only origin main`. For a new checkout, run `git clone https://github.com/Virtuoso1/DevStudio.git`.

1. Start PostgreSQL and create a database named `homeservices` if it does not exist.
2. From the project root, open PowerShell and run the backend setup. Copy `.env.example` only when there is no existing `.env`. Set `DATABASE_URL` to your PostgreSQL credentials and `JWT_SECRET` to a long random string.

   ```powershell
   cd backend
   if (!(Test-Path .env)) { Copy-Item .env.example .env }
   # Edit .env before continuing.
   npm.cmd ci
   npx.cmd prisma migrate deploy
   npm.cmd run seed
   npm.cmd run dev
   ```

   On macOS or Linux, use this from the project root:

   ```bash
   cd backend
   test -e .env || cp .env.example .env
   # Edit .env before continuing.
   npm ci
   npx prisma migrate deploy
   npm run seed
   npm run dev
   ```

   Seeding is optional and creates customer and provider demo accounts. Check [API health](http://localhost:3000/health) for `"database":"connected"`. Keep the API terminal open. [Swagger](http://localhost:3000/docs) lists the routes.

3. Open the project root in Android Studio, install the Android 37 SDK platform specified in `app/build.gradle.kts`, and sync Gradle using JDK 17. Start an Android emulator with API 26 or higher. The emulator reaches the host API at `http://10.0.2.2:3000/`.
4. Select the emulator and run the `app` configuration. To build from a separate PowerShell at the project root, run `.\gradlew.bat :app:assembleDebug`. On macOS or Linux, use `./gradlew :app:assembleDebug`. Set `ANDROID_HOME` to the SDK directory or let Android Studio create `local.properties`.

## Verify the main flows

- Sign in with `demo@homeservices.test` and `Password123`, or use **Create an account**.
- Choose **Plumber**, open **Grace Wanjiku**, and request a booking for a future slot. Enter at least five characters describing the job. Open **Bookings** to check that the status is **Pending**.
- Sign out from **My Profile**, then sign in as `grace@homeservices.test` with the same demo password. Open **Bookings**, confirm the request, then mark it completed.
- Sign back in as the customer and leave a review. The provider profile shows saved reviews. Completed providers also appear under **Book again**.
- From the provider profile, start a conversation as a customer. Sign in as the provider and open **Messages** to see or reply to it.

The home search uses the Node API for trade, name, and service searches. The documented local flows use the Android app and the Node/PostgreSQL backend.

## Backend tests

Configure `backend/.env.test` with a separate PostgreSQL database named `homeservices_test` and a test JWT secret. From `backend`, run `npm.cmd run test:migrate` followed by `npm.cmd test`. The test suite clears records from the test database.
