export type Role = "student" | "moderator" | "admin";

export interface ProfileRow {
  id: string;
  full_name: string;
  email: string;
  role: Role;
  institution_name: string;
  department: string;
  course: string;
  year_of_study: number;
  verified: boolean;
  suspended: boolean;
  created_at: string;
}

export interface PostRow {
  id: string;
  author_name: string;
  content: string;
  status: string;
  like_count: number;
  comment_count: number;
  created_at: string;
}

export interface ListingRow {
  id: string;
  seller_name: string;
  title: string;
  price_kes: number;
  category: string;
  status: string;
  created_at: string;
}

export interface OpportunityRow {
  id: string;
  title: string;
  organization: string;
  type: string;
  deadline: string;
  status: string;
  created_at: string;
}

export interface ReportRow {
  id: string;
  content_type: string;
  content_id: string;
  reason: string;
  details: string;
  status: string;
  created_at: string;
}

export interface AnnouncementRow {
  id: string;
  title: string;
  body: string;
  audience: string;
  status: string;
  published_at: string;
}

export interface DashboardData {
  stats: {
    users: number;
    posts: number;
    pendingListings: number;
    openReports: number;
    opportunities: number;
    applications: number;
  };
  recentReports: ReportRow[];
  pendingListings: ListingRow[];
}

export interface StaffSession {
  id: string;
  email: string;
  fullName: string;
  role: Role;
  demo: boolean;
}
