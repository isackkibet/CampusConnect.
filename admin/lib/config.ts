export const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL ?? "";
export const supabaseAnonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY ?? "";

export const isDemoMode = !supabaseUrl || !supabaseAnonKey;

export const DEMO_COOKIE = "cc-admin-demo";
