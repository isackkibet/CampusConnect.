"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { createSupabaseServerClient } from "@/lib/supabase/server";
import { DEMO_COOKIE, isDemoMode } from "@/lib/config";

export async function enterDemoMode() {
  if (!isDemoMode) redirect("/login?error=Demo+mode+is+disabled+because+Supabase+is+configured");
  (await cookies()).set(DEMO_COOKIE, "1", {
    httpOnly: true,
    sameSite: "lax",
    secure: process.env.NODE_ENV === "production",
    path: "/",
    maxAge: 60 * 60 * 8,
  });
  redirect("/dashboard");
}

export async function signIn(formData: FormData) {
  if (isDemoMode) redirect("/login?error=Configure+Supabase+to+use+real+staff+sign-in");

  const email = String(formData.get("email") ?? "").trim();
  const password = String(formData.get("password") ?? "");
  if (!email || !password) redirect("/login?error=Email+and+password+are+required");

  const supabase = await createSupabaseServerClient();
  const { data, error } = await supabase.auth.signInWithPassword({ email, password });
  if (error || !data.user) {
    redirect(`/login?error=${encodeURIComponent(error?.message ?? "Could not sign in")}`);
  }

  const { data: profile } = await supabase
    .from("profiles")
    .select("role,suspended")
    .eq("id", data.user.id)
    .maybeSingle();

  if (!profile || profile.suspended || (profile.role !== "admin" && profile.role !== "moderator")) {
    await supabase.auth.signOut();
    redirect("/login?error=Staff+access+required.+Ask+an+administrator+to+grant+your+role.");
  }

  redirect("/dashboard");
}

export async function signOut() {
  if (!isDemoMode) {
    const supabase = await createSupabaseServerClient();
    await supabase.auth.signOut();
  }
  (await cookies()).delete(DEMO_COOKIE);
  redirect("/login");
}
