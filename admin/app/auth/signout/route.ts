import { NextResponse } from "next/server";
import { createSupabaseServerClient } from "@/lib/supabase/server";
import { DEMO_COOKIE, isDemoMode } from "@/lib/config";

export async function POST(request: Request) {
  if (!isDemoMode) {
    const supabase = await createSupabaseServerClient();
    await supabase.auth.signOut();
  }
  const response = NextResponse.redirect(new URL("/login", request.url));
  response.cookies.delete(DEMO_COOKIE);
  return response;
}
