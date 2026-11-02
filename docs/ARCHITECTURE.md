# Architecture

## Android

Package root: `com.campusconnect.app`.

```
core/
  design/          Material 3 color, type and theme
  model/           Serializable domain models and report options
  data/            ServiceConfig, DemoData, DemoStore, BackendApi
  data/local/      Room entities, DAOs, CampusDatabase
  data/repo/       Session, community, notifications, marketplace, opportunities,
                   accommodation, events, messaging, map, assistant, payment repositories
  di/              Hilt module (DataStore, Room, Ktor, scopes)
  notifications/   FCM service and channel helper
  util/            Validators, TimeFormat
features/          One package per feature: ViewModel + Compose screen(s)
navigation/        Routes, RootViewModel, AppRoot NavHost + bottom bar
ui/components/     Reusable cards, buttons, states, avatar, report dialog, banner
```

- **UI:** Jetpack Compose only, Material 3, light/dark palettes. Screens collect immutable
  `StateFlow` UI state with `collectAsStateWithLifecycle`.
- **State:** `ViewModel` + `StateFlow`; Hilt provides repositories. `SavedStateHandle` supplies route
  arguments (`postId`, `conversationId`, `listingId`, `opportunityId`, `accommodationId`).
- **Persistence:** DataStore stores onboarding/session/profile/notification preferences. Room stores
  posts, comments, notifications and saved items. DemoStore keeps the remaining sample features
  in memory so every action is functional offline and resets on process death.
- **Navigation:** a single `NavHost`. `AppRoot` derives a target destination from session state
  (onboarding → login → profile setup → home) and only redirects from auth/splash routes, so editing
  a profile or opening a detail screen is not interrupted. Bottom navigation appears on the five main
  tabs.
- **Configuration:** `ServiceConfig` reads `BuildConfig` values generated from `local.properties`.
  Missing credentials activate labelled demo/offline paths rather than fake success.

## Local-first demo boundaries

- Community and notification writes persist across restarts (Room + DataStore).
- Marketplace/opportunities/accommodation/events/messaging are sample in-memory data with real
  mutations and simulated auto-replies; they reset on process death.
- All people, employers, prices, events and map coordinates are fictional sample content.
- Payment status is never set by the client. The app only displays a pending checkout request if a
  secure backend accepts it; success must arrive from the Daraja callback.

## Admin dashboard

Next.js 14 App Router + TypeScript + Tailwind. Server Components read Supabase with the caller's
cookie session; RLS applies. The `/api/admin/action` route re-checks the caller's staff role and then
uses the service-role key for privileged writes (role changes, moderation, announcements). Requests
with `Sec-Fetch-Site: cross-site` are rejected. In demo mode, no service key is needed and actions
return a "simulated, not saved" result.

## Supabase backend

- `supabase/migrations/0001_init.sql` creates reference data, profiles, community, marketplace,
  opportunities, accommodation, events, notifications, announcements, saved items, reports, device
  tokens, payments and AI usage. RLS is enabled on every table with owner/staff policies and column
  guards that stop self-promotion, self-verification and self-approval.
- Storage buckets: `avatars`, `posts`, `listings`, `accommodation`; owners can write under their
  user-id folder, public read.
- Edge functions: `groq-assistant` (JWT + per-minute rate limit), `mpesa-stk-push` (JWT, validation,
  pending-only insert, Daraja call) and `mpesa-callback` (shared secret, callback-only status update).
- Messaging is deliberately not modelled in Postgres; Stream Chat/Video is the intended provider.
