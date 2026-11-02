# Local setup

## 1. Android app

Requirements: Android Studio with JDK 17+ (the bundled JBR works), Android SDK 36, and an
emulator/device on API 26+.

1. Copy/open the repository in Android Studio and let Gradle sync.
2. `local.properties` must contain `sdk.dir`. Optional integration keys can be left blank.
3. Build, test and install:

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:installDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

### Demo account

- Tap **Continue with demo account** on the login screen. This seeds a complete profile.
- Or register with any valid email and a password of 8+ characters containing a letter and digit.
- Verification code: **123456**. No email is sent in demo mode.

Data is local: Room persists posts/comments/notifications; other features use an in-memory store
reset on process death. All sample people/employers/listings are fictional.

### Optional Android configuration

Add to `local.properties` or pass as Gradle properties/environment variables:

| Key | Purpose |
| --- | --- |
| `MAPS_API_KEY` | Reserved for live Maps tiles; schematic map works without it. |
| `SUPABASE_URL` / `SUPABASE_ANON_KEY` | Reserved for remote data/auth wiring. |
| `STREAM_API_KEY` | Reserved for Stream Chat/Video; not yet implemented. |
| `BACKEND_BASE_URL` | Supabase Functions base URL, e.g. `https://<ref>.supabase.co`. |
| `ADMIN_BASE_URL` | Link opened from Profile → Admin dashboard. |

If `BACKEND_BASE_URL` is set, the AI guide calls `functions/v1/groq-assistant`. The Android client
does not yet attach a Supabase user JWT, so the JWT-protected function will reject it until Supabase
Auth is wired into the app. Keep it blank to use the offline guide.

## 2. Admin dashboard

Requirements: Node.js 20+.

```powershell
cd admin
npm install
npm run dev
```

Open `http://localhost:3000`.

- No environment variables → demo mode with fictional data; actions are simulated.
- `admin/.env.example` lists the Supabase variables. Use an **anon** key in `NEXT_PUBLIC_*` and keep
  `SUPABASE_SERVICE_ROLE_KEY` server-only.
- Sign in with an account whose `profiles.role` is `admin` or `moderator`.

Useful checks:

```powershell
npm run typecheck
npm run build
```

## 3. Supabase backend (optional)

Requirements: Supabase CLI, Docker, a Supabase project.

```powershell
supabase login
supabase link --project-ref <project-ref>
supabase db push
supabase functions deploy groq-assistant
supabase functions deploy mpesa-stk-push
supabase functions deploy mpesa-callback
```

Then set function secrets (never commit them):

```powershell
supabase secrets set GROQ_API_KEY=... GROQ_MODEL=llama-3.3-70b-versatile
```

See [`DEPLOYMENT.md`](DEPLOYMENT.md) for the full secret list, M-PESA callback setup and admin
deployment. To make the first admin, sign up a user, then update their role in the SQL editor:

```sql
update public.profiles set role = 'admin' where email = 'staff@example.com';
```

## Troubleshooting

- **Gradle cannot find the SDK** – set `sdk.dir` in `local.properties`.
- **Java version error** – point `JAVA_HOME` at Android Studio's JBR or a JDK 17+.
- **Admin actions fail with 500** – set `SUPABASE_SERVICE_ROLE_KEY` on the server.
- **Admin login says staff access required** – promote the account role in `profiles`.
- **AI assistant says offline** – that is expected without a backend; the offline guide is intentional.
