import { redirect } from "next/navigation";
import { getStaffSession } from "@/lib/auth";
import { isDemoMode } from "@/lib/config";
import { enterDemoMode, signIn } from "./actions";

export default async function LoginPage({
  searchParams,
}: {
  searchParams: Promise<{ error?: string }>;
}) {
  const [{ error }, staff] = await Promise.all([searchParams, getStaffSession()]);
  if (staff) redirect("/dashboard");

  return (
    <main className="flex min-h-screen items-center justify-center bg-navy-950 px-4 py-12">
      <div className="w-full max-w-md">
        <div className="mb-6 text-center">
          <p className="text-2xl font-bold tracking-tight text-white">CampusConnect</p>
          <p className="text-sm text-slate-400">Staff administration console</p>
        </div>

        <div className="card space-y-5 p-6">
          {isDemoMode ? (
            <div className="rounded-xl border border-amber-300 bg-amber-50 p-4 text-sm text-amber-900">
              <strong className="font-semibold">Demo mode.</strong> Supabase environment variables are
              not set. This console uses fictional sample data; moderation actions are simulated and
              not saved. Do not treat any content as official campus information.
            </div>
          ) : null}

          {error ? (
            <div className="rounded-xl border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700">
              {error}
            </div>
          ) : null}

          {isDemoMode ? (
            <form action={enterDemoMode}>
              <button type="submit" className="btn btn-primary w-full py-2.5 text-sm">
                Continue in demo mode
              </button>
            </form>
          ) : (
            <form action={signIn} className="space-y-4">
              <div>
                <label htmlFor="email" className="mb-1 block text-xs font-semibold text-slate-600">
                  Staff email
                </label>
                <input
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  required
                  className="field"
                  placeholder="you@institution.ac.ke"
                />
              </div>
              <div>
                <label htmlFor="password" className="mb-1 block text-xs font-semibold text-slate-600">
                  Password
                </label>
                <input
                  id="password"
                  name="password"
                  type="password"
                  autoComplete="current-password"
                  required
                  className="field"
                  placeholder="Your password"
                />
              </div>
              <button type="submit" className="btn btn-primary w-full py-2.5 text-sm">
                Sign in
              </button>
              <p className="text-center text-xs text-slate-500">
                Access is limited to accounts with the admin or moderator role.
              </p>
            </form>
          )}
        </div>
      </div>
    </main>
  );
}
