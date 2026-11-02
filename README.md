# CampusConnect

CampusConnect is a student super-app for Kenyan tertiary institutions. The repository contains:

- **`app/`** – a Kotlin/Jetpack Compose Android app (MVVM, Hilt, Room, DataStore, Ktor).
- **`admin/`** – a Next.js 14 staff dashboard for moderation and announcements.
- **`supabase/`** – SQL schema, RLS policies, storage buckets and Deno edge functions.
- **`docs/`** – setup, architecture, deployment and security notes.

## What works today

The Android app runs end-to-end **offline in local-first demo mode** with no credentials:

- 3-page onboarding, demo sign-in, registration + demo email verification (`123456`), profile setup/edit.
- Home dashboard, community feed with groups/students tabs, create/edit/delete/like/comment/report posts.
- Marketplace with search, categories, saved items, listing detail, contact-seller chat and a local
  moderation queue (new listings become active after a few seconds).
- Opportunities with type filters, save, apply and application tracking; staff-only publishing.
- Accommodation directory with rent/availability filters, detail and phone dialer.
- Events with RSVP/reminder state, campus point-of-interest schematic map + external Google Maps intent.
- Messaging UI with simulated replies and typing indicators.
- Notifications with preferences, saved items, applications, AI guide (offline deterministic answers).
- Light/dark Material 3 theme, bottom navigation for Home / Community / Marketplace / Opportunities / Profile.

All seeded students, employers, listings, rooms, events and map coordinates are **clearly labelled
sample/fictional data**. They are not official institutional information.

## What is credential-dependent (not yet enabled end-to-end)

| Integration | State in this repository |
| --- | --- |
| Supabase Auth + remote data | Schema/RLS exist; Android still uses the local demo session. |
| Supabase Storage | Buckets/policies exist; Android image picker shows local URIs only. |
| Stream Chat / Video | Not implemented; chat is simulated locally and calls show a labelled notice. |
| Google Maps | Schematic coordinate plot; `MAPS_API_KEY` is not consumed for live tiles. |
| FCM push | Service exists; device-token registration and server send job are not wired. |
| Groq assistant | Edge function exists and is secure; Android uses offline answers unless `BACKEND_BASE_URL` is set. |
| M-PESA | Secure STK/callback functions exist; no Android payment UI and no client-side success claims. |
| Admin dashboard | Runs immediately in demo mode; real Supabase mode is implemented but untested against a live project. |

See [`docs/DEVELOPMENT_STATUS.md`](docs/DEVELOPMENT_STATUS.md) for the honest feature matrix.

## Requirements

- Android Studio (JDK 17+) and Android SDK 36; device/emulator on API 26+.
- Node.js 20+ for the admin dashboard.
- Supabase CLI + Docker (optional, for deploying the backend).

## Quick start – Android (demo mode)

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

Install `app/build/outputs/apk/debug/app-debug.apk`, or run from Android Studio.
No credentials are needed. Use **Continue with demo account** on the login screen, or register and
use verification code `123456`.

## Quick start – admin dashboard (demo mode)

```powershell
cd admin
npm install
npm run dev
```

Open `http://localhost:3000`. With no environment variables it runs in clearly-labelled demo mode
with fictional data; moderation actions are simulated and not saved. See `admin/.env.example` to
connect Supabase. Never expose the service-role key to the browser.

## Configuration

Android values are injected into `BuildConfig` from `local.properties` (or Gradle properties /
environment variables). Leave them blank for demo mode:

```properties
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=
SUPABASE_URL=
SUPABASE_ANON_KEY=
STREAM_API_KEY=
BACKEND_BASE_URL=
ADMIN_BASE_URL=https://example.invalid/admin
```

Server secrets (Supabase functions) are documented in [`docs/DEPLOYMENT.md`](docs/DEPLOYMENT.md).
**Never commit** `local.properties`, `.env` files, service-role keys, `google-services.json` or signing
keystores.

## Project layout

```text
app/                    Android application
  src/main/java/com/campusconnect/app/
    core/               design, models, data, repositories, DI, notifications, utils
    features/           auth, onboarding, home, community, marketplace, opportunities,
                        accommodation, events, map, messaging, notifications, profile, assistant
    navigation/         routes, root navigation, auto session routing
admin/                  Next.js staff dashboard
supabase/
  migrations/           schema, RLS, storage policies, guards, seed reference data
  functions/            groq-assistant, mpesa-stk-push, mpesa-callback
docs/                   setup, architecture, deployment, security, status
```

## Documentation

- [`docs/SETUP.md`](docs/SETUP.md) – local setup for all three parts.
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) – layers, data flow, demo-mode boundaries.
- [`docs/DEPLOYMENT.md`](docs/DEPLOYMENT.md) – Supabase, edge functions, admin, Android release.
- [`docs/SECURITY.md`](docs/SECURITY.md) – auth, RLS, secrets, payments, moderation.
- [`docs/DEVELOPMENT_STATUS.md`](docs/DEVELOPMENT_STATUS.md) – implemented vs. planned.

## Contribute

Open an issue or focused pull request. Run `:app:testDebugUnitTest`, `:app:assembleDebug` and
`cd admin && npm run typecheck && npm run build` before submitting. Never include secrets.
