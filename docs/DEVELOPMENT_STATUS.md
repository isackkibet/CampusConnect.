# Development status

Last updated: 2026-11-02

## Verified in this environment

- `:app:compileDebugKotlin` — passes.
- `:app:assembleDebug` — passes; APK at `app/build/outputs/apk/debug/app-debug.apk`.
- `:app:testDebugUnitTest` — 9 tests pass (`ValidatorsTest`, `TimeFormatTest`).
- `admin` `npm run typecheck` — passes.
- `admin` `npm run build` — passes (Next.js 14.2.35, 14 routes).
- Supabase SQL/functions were **not executed** against a live/local Supabase instance (CLI/Docker not
  available here), so schema and policy runtime behaviour still needs verification with `supabase db push`.

## Android feature matrix

| Area | State | Notes |
| --- | --- | --- |
| Onboarding, login, register, demo verify, reset password UI | Implemented (demo) | Local DataStore session, code `123456`, no real email. |
| Profile setup/edit, notification preferences, sign out | Implemented | Validation, department/course/year, bio/phone. |
| Home dashboard | Implemented | Greeting, announcements, recent posts, events, opportunities, listings. |
| Community feed, groups, students, create/edit/delete/like/comment/report/share | Implemented | Room-backed text/image URI posts; share is a no-op. |
| Marketplace list/detail/create, save, report, mark sold, delete, contact seller | Implemented (demo) | Local auto-approval queue and simulated seller chat. |
| Opportunities list/detail/apply, save, staff publish | Implemented (demo) | Application records local; publish role-gated locally. |
| Accommodation list/detail/filter/contact/report | Implemented (demo) | Phone dialer intent; no in-app booking/payment. |
| Events list/RSVP/reminder | Implemented (demo) | No system calendar or push reminders. |
| Campus map | Partial | Coordinate schematic + filters + Google Maps directions intent; no live tiles even with key. |
| Messaging | Partial | Conversation list/chat/send/image attach/typing; simulated replies, not real-time. |
| Calls | Not implemented | Top-bar buttons show an honest "requires Stream" notice. |
| AI assistant | Partial | Offline deterministic guide by default; secure backend call exists but Android lacks a Supabase JWT. |
| Notifications | Partial | Room-backed list/preferences; FCM service exists but token registration and sending are missing. |
| Payments | Backend only | Secure STK/callback functions included; no Android payment screen. |
| Supabase auth/data sync | Not wired | Android uses demo repositories; schema is ready. |
| Theme/accessibility | Implemented | Material 3 light/dark, content descriptions, large touch targets; not formally audited. |
| Localization | Not implemented | English only, Kenyan context. |

## Admin dashboard

| Area | State |
| --- | --- |
| Demo mode with fictional data and simulated actions | Implemented |
| Supabase cookie auth + staff role check | Implemented, not tested against live Supabase |
| Overview metrics, moderation queues | Implemented |
| Users: verify, suspend/restore, role changes (admin-only) | Implemented |
| Posts: publish/hide/delete | Implemented |
| Listings: approve/reject/mark sold | Implemented |
| Opportunities: publish/close | Implemented |
| Reports: resolve/dismiss | Implemented |
| Announcements: publish/list | Implemented; no push delivery |
| Tests / CI | None |

## Backend

| Area | State |
| --- | --- |
| Schema, RLS, column guards, counters, storage buckets, sample reference data | Written; not executed here |
| `groq-assistant` (JWT, suspension, rate limit, secret isolation) | Written; not deployed/tested |
| `mpesa-stk-push` (JWT, validation, pending insert, Daraja) | Written; not deployed/tested |
| `mpesa-callback` (shared secret, status update) | Written; not deployed/tested |
| FCM send job / scheduled reminders | Not implemented |
| Stream token endpoint | Not implemented |

## Known issues and risks

1. Android does not yet persist or refresh Supabase sessions; backend calls have no user JWT.
2. Demo repositories reset on process death except Room-backed posts/comments/notifications/saved.
3. In-memory auto-moderation timers (`DemoStore`) are intentional demo behaviour, not production rules.
4. Supabase migration must be validated; storage policy names can conflict if rerun manually.
5. Admin dashboard has no automated tests and no CSRF token beyond same-site cookies + fetch-site check.
6. FCM, live Maps, Stream, payment UI and push delivery remain unfinished.
7. No CI, crash reporting, analytics, accessibility audit, localization or store assets.

## Recommended next steps

1. Run the migration locally (`supabase start && supabase db push`), test RLS as student/moderator/admin.
2. Add Supabase Auth session handling to Android and migrate repositories table-by-table, keeping demo fallback.
3. Implement Storage uploads and FCM token registration plus a notification send job.
4. Add Stream token function and real Chat/Video, or remove the call buttons until ready.
5. Add Maps Compose with key gating, and payment UI that only reflects callback-confirmed status.
6. Add integration tests for repositories, Compose UI tests for critical flows, and CI (Gradle + npm).
7. Complete privacy/retention/consent documentation and store release assets.
