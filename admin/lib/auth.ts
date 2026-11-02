import { cookies } from "next/headers";
import type { StaffSession, Role } from "@/lib/types";
import { createSupabaseServerClient } from "@/lib/supabase/server";
import { DEMO_COOKIE, isDemoMode } from "@/lib/config";

export async function getStaffSession(): Promise<StaffSession | null> {
  if (isDemoMode) {
    const demo = (await cookies()).get(DEMO_COOKIE)?.value === "1";
    if (!demo) return null;
    return {
      id: "demo-admin",
      email: "demo.admin@campusconnect.local",
      fullName: "Demo Administrator",
      role: "admin",
      demo: true,
    };
  }

  const supabase = await createSupabaseServerClient();
  const {
    data: { user },
  } = await supabase.auth.getUser();
  if (!user) return null;

  const { data: profile } = await supabase
    .from("profiles")
    .select("id,email,full_name,role,suspended")
    .eq("id", user.id)
    .maybeSingle();

  if (!profile || profile.suspended) return null;
  const role = profile.role as Role;
  if (role !== "admin" && role !== "moderator") return null;

  return {
    id: profile.id,
    email: profile.email || user.email || "",
    fullName: profile.full_name || "Staff member",
    role,
    demo: false,
  };
}
