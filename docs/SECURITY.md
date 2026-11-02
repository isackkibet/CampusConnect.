# Security

## Principles

- Secrets stay server-side. The Android app only holds public keys (Supabase anon, Maps, Stream
  public key). Daraja, Groq, service-role and signing secrets must never ship in the APK or browser.
- Never fake success. Payment status, verification, moderation and delivery outcomes are reported
  only after a trusted backend confirms them.
- Least privilege. RLS is enabled on every table; staff actions are re-checked server-side.

## Authentication and authorization

- Android currently uses **local demo sessions** (DataStore). There is no password storage; login
  accepts valid-looking credentials and the demo verification code is `123456`. This is not real auth.
- Production must use Supabase Auth, verify email, and attach the user's access token to backend
  calls. Do not ship the demo session as production authentication.
- Admin access requires `profiles.role in ('admin','moderator')` and `suspended = false`. The
  dashboard layout checks it, and `/api/admin/action` checks it again before using the service role.
- Column-guard triggers prevent users from changing their own role, verification, suspension,
  ownership, counters, or approving their own posts/listings/accommodation. Sellers may only move
  their own active listing to `sold`.

## Row Level Security highlights

- Profiles: self or staff read; self edit with guards.
- Posts/comments: published content is readable; authors and staff can update/delete; status and
  ownership guarded.
- Listings: active/sold readable; sellers insert as `pending_review` and cannot self-approve.
- Opportunities/events/announcements: published readable; staff-only writes.
- Applications/saved items/notifications/device tokens/RSVPs: owner-scoped.
- Reports: anyone signed in can file; only staff can read the queue or resolve.
- Payments: owner/staff read only; no client insert/update policies. Functions use the service role.
- Storage: public read for image buckets; writes/deletes restricted to the owner's `<uid>/` folder.

## Edge functions

- `groq-assistant` and `mpesa-stk-push` require a valid Supabase JWT and check suspension. The
  assistant rate-limits to 10 requests/minute/user and caps prompt/history length. The system prompt
  forbids inventing official policies and requesting secrets.
- `mpesa-callback` disables JWT verification because Daraja cannot send one; it requires a
  high-entropy shared secret in the callback URL, compared in constant time, and runs with the
  service role only to update the matching payment. Rotate the secret if leaked.
- CORS is permissive for mobile clients; do not rely on CORS as authorization.

## Payments

- The Android client never calculates or sets payment success. `mpesa-stk-push` inserts `pending`,
  calls Daraja, and returns the checkout id. Only the callback sets `success`/`failed`.
- Validate amount, phone, purpose and reference server-side. Reconcile with the M-PESA dashboard and
  keep an audit trail before enabling real money.

## Content and privacy

- Report, hide and moderation flows exist. Moderators must investigate before deleting content.
- Sample data is fictional. Do not advertise it as official campus information.
- Minimise personal data: no passwords, PINs, ID scans or payment secrets are requested. Add a
  retention/deletion policy, export process and consent notices before launch.

## Operational checklist

- Rotate any key that was ever committed or shared in chat.
- Use separate Supabase projects for development and production.
- Restrict service-role keys to server environments.
- Enable Supabase logs/alerts and database backups.
- Add dependency scanning, CI checks (`assembleDebug`, unit tests, `npm run build`) and crash
  reporting.
- Perform a privacy impact assessment and, where applicable, comply with Kenya's Data Protection Act.
