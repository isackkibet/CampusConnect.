# Deployment

## Supabase

1. Create a project, then link and push the schema:

```powershell
supabase link --project-ref <project-ref>
supabase db push
```

2. Confirm RLS is enabled on every public table and review policies before exposing any key.
3. Promote the first staff account:

```sql
update public.profiles set role = 'admin' where email = 'staff@example.com';
```

4. Enable email confirmation in Supabase Auth for production; the Android demo verification is not
   a substitute. Point the Android auth layer at Supabase before treating any session as real.

### Storage

Buckets and owner-folder policies are created by the migration. Upload paths must start with
`<auth.uid()>/...`. Review public-read requirements before launch; moderation and takedown must also
remove the underlying object.

## Edge functions

```powershell
supabase functions deploy groq-assistant
supabase functions deploy mpesa-stk-push
supabase functions deploy mpesa-callback
```

`supabase/functions/config.toml` disables JWT verification only for `mpesa-callback`.

### Assistant secrets

| Secret | Required | Notes |
| --- | --- | --- |
| `GROQ_API_KEY` | Yes | Server-only Groq key. |
| `GROQ_MODEL` | No | Defaults to `llama-3.3-70b-versatile`. |
| `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY` | Yes | Injected by Supabase. |

The function verifies the caller JWT, blocks suspended profiles, limits each user to 10 requests per
minute via `ai_usage`, caps message size/history, and never exposes the Groq key.

### M-PESA secrets

| Secret | Required | Notes |
| --- | --- | --- |
| `MPESA_CONSUMER_KEY` / `MPESA_CONSUMER_SECRET` | Yes | Daraja app credentials. |
| `MPESA_SHORTCODE` | Yes | Paybill/till short code. |
| `MPESA_PASSKEY` | Yes | Daraja STK passkey. |
| `MPESA_CALLBACK_URL` | Yes | `https://<ref>.supabase.co/functions/v1/mpesa-callback?secret=<secret>` |
| `MPESA_CALLBACK_SECRET` | Yes | Long random shared secret; required by the callback. |
| `MPESA_ENV` | No | `sandbox` (default) or `production`. |

The app must never receive Daraja credentials. `mpesa-stk-push` records a `pending` payment and
returns the checkout request id. Only `mpesa-callback` (authenticated by the shared secret) may set
`success`/`failed`. Test with the Daraja sandbox and reconcile against the M-PESA dashboard.

## Admin dashboard

Deploy `admin/` to Vercel (or any Node host):

1. Set `NEXT_PUBLIC_SUPABASE_URL`, `NEXT_PUBLIC_SUPABASE_ANON_KEY`.
2. Set `SUPABASE_SERVICE_ROLE_KEY` as a **server-only** secret.
3. Build with `npm run build`; start with `npm run start`.
4. Restrict staff access through `profiles.role`; there is no separate admin password.

Demo mode is only for evaluation. Never run a public production deployment without Supabase env
variables and service-role protection.

## Android release

1. Create a signing keystore and add a `release` signing config locally; never commit it.
2. Fill `local.properties` (or CI secrets) with `SUPABASE_URL`, `SUPABASE_ANON_KEY`,
   `BACKEND_BASE_URL`, optional `MAPS_API_KEY` and `ADMIN_BASE_URL`.
3. Add `app/google-services.json` for FCM; register device tokens (server send job still required).
4. Build `:app:assembleRelease` and verify minification rules.
5. Complete the gaps in `DEVELOPMENT_STATUS.md` before claiming production readiness: real Supabase
   auth/data sync, Stream messaging/calls, live Maps tiles, FCM registration/sending, and payment UI.

## Known deployment gaps

- Android sessions are local demo sessions; capturing Supabase sessions and refreshing JWTs is not
  implemented yet.
- Retry/idempotency for M-PESA callbacks is minimal (update by checkout id; repeated callbacks are
  safe but are not individually audited).
- No CI pipelines, crash reporting, analytics or store listing assets are included.
