import { isDemoMode } from "@/lib/config";
import { createSupabaseServerClient } from "@/lib/supabase/server";
import {
  demoAnnouncements,
  demoDashboard,
  demoListings,
  demoOpportunities,
  demoPosts,
  demoProfiles,
  demoReports,
} from "@/lib/demo-data";
import type {
  AnnouncementRow,
  DashboardData,
  ListingRow,
  OpportunityRow,
  PostRow,
  ProfileRow,
  ReportRow,
} from "@/lib/types";

async function count(table: string, filter?: (query: any) => any): Promise<number> {
  const supabase = await createSupabaseServerClient();
  let query = supabase.from(table).select("id", { count: "exact", head: true });
  if (filter) query = filter(query);
  const { count: value } = await query;
  return value ?? 0;
}

export async function getDashboardData(): Promise<DashboardData> {
  if (isDemoMode) return demoDashboard;
  const supabase = await createSupabaseServerClient();
  const [users, posts, pendingListings, openReports, opportunities, applications] =
    await Promise.all([
      count("profiles"),
      count("posts"),
      count("listings", (q) => q.eq("status", "pending_review")),
      count("reports", (q) => q.eq("status", "open")),
      count("opportunities"),
      count("applications"),
    ]);

  const { data: recentReports } = await supabase
    .from("reports")
    .select("*")
    .eq("status", "open")
    .order("created_at", { ascending: false })
    .limit(5);

  const { data: pending } = await supabase
    .from("listings")
    .select("*")
    .eq("status", "pending_review")
    .order("created_at", { ascending: true })
    .limit(5);

  return {
    stats: {
      users,
      posts,
      pendingListings,
      openReports,
      opportunities,
      applications,
    },
    recentReports: (recentReports ?? []) as ReportRow[],
    pendingListings: (pending ?? []) as ListingRow[],
  };
}

export async function getUsers(): Promise<ProfileRow[]> {
  if (isDemoMode) return demoProfiles;
  const supabase = await createSupabaseServerClient();
  const { data } = await supabase
    .from("profiles")
    .select("*")
    .order("created_at", { ascending: false })
    .limit(100);
  return (data ?? []) as ProfileRow[];
}

export async function getPosts(): Promise<PostRow[]> {
  if (isDemoMode) return demoPosts;
  const supabase = await createSupabaseServerClient();
  const { data } = await supabase
    .from("posts")
    .select("*")
    .order("created_at", { ascending: false })
    .limit(100);
  return (data ?? []) as PostRow[];
}

export async function getListings(): Promise<ListingRow[]> {
  if (isDemoMode) return demoListings;
  const supabase = await createSupabaseServerClient();
  const { data } = await supabase
    .from("listings")
    .select("*")
    .order("created_at", { ascending: false })
    .limit(100);
  return (data ?? []) as ListingRow[];
}

export async function getOpportunities(): Promise<OpportunityRow[]> {
  if (isDemoMode) return demoOpportunities;
  const supabase = await createSupabaseServerClient();
  const { data } = await supabase
    .from("opportunities")
    .select("*")
    .order("created_at", { ascending: false })
    .limit(100);
  return (data ?? []) as OpportunityRow[];
}

export async function getReports(): Promise<ReportRow[]> {
  if (isDemoMode) return demoReports;
  const supabase = await createSupabaseServerClient();
  const { data } = await supabase
    .from("reports")
    .select("*")
    .order("created_at", { ascending: false })
    .limit(100);
  return (data ?? []) as ReportRow[];
}

export async function getAnnouncements(): Promise<AnnouncementRow[]> {
  if (isDemoMode) return demoAnnouncements;
  const supabase = await createSupabaseServerClient();
  const { data } = await supabase
    .from("announcements")
    .select("*")
    .order("published_at", { ascending: false })
    .limit(100);
  return (data ?? []) as AnnouncementRow[];
}
