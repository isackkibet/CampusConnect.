import type {
  AnnouncementRow,
  DashboardData,
  ListingRow,
  OpportunityRow,
  PostRow,
  ProfileRow,
  ReportRow,
} from "@/lib/types";

const now = Date.now();
const ago = (hours: number) => new Date(now - hours * 3_600_000).toISOString();
const ahead = (days: number) => new Date(now + days * 86_400_000).toISOString();

export const demoProfiles: ProfileRow[] = [
  { id: "u-1", full_name: "Mary Achieng (demo)", email: "mary.demo@campusconnect.local", role: "student", institution_name: "Nairobi National Polytechnic (sample)", department: "Computing and Informatics (sample)", course: "Information Technology", year_of_study: 2, verified: true, suspended: false, created_at: ago(72) },
  { id: "u-2", full_name: "Amina Otieno (demo)", email: "amina.demo@campusconnect.local", role: "student", institution_name: "Nairobi National Polytechnic (sample)", department: "Business Studies (sample)", course: "Supply Chain", year_of_study: 3, verified: true, suspended: false, created_at: ago(52) },
  { id: "u-3", full_name: "Brian Mwangi (demo)", email: "brian.demo@campusconnect.local", role: "moderator", institution_name: "Nairobi National Polytechnic (sample)", department: "Computing and Informatics (sample)", course: "Software Engineering", year_of_study: 4, verified: true, suspended: false, created_at: ago(240) },
  { id: "u-4", full_name: "Faith Wanjiku (demo)", email: "faith.demo@campusconnect.local", role: "student", institution_name: "Nairobi National Polytechnic (sample)", department: "Engineering (sample)", course: "Electrical Engineering", year_of_study: 1, verified: false, suspended: false, created_at: ago(8) },
  { id: "u-5", full_name: "Kevin Oduor (demo)", email: "kevin.demo@campusconnect.local", role: "student", institution_name: "Nairobi National Polytechnic (sample)", department: "Business Studies (sample)", course: "Accounting", year_of_study: 2, verified: true, suspended: true, created_at: ago(300) },
];

export const demoPosts: PostRow[] = [
  { id: "p-1", author_name: "Amina Otieno (demo)", content: "Study group for Database Systems meets at the library every Tuesday at 5pm. All welcome.", status: "published", like_count: 18, comment_count: 4, created_at: ago(3) },
  { id: "p-2", author_name: "Brian Mwangi (demo)", content: "Reminder: the sample hackathon registration closes on Friday. Teams of up to four.", status: "published", like_count: 32, comment_count: 9, created_at: ago(7) },
  { id: "p-3", author_name: "Kevin Oduor (demo)", content: "Selling a second-hand calculator, barely used. DM if interested.", status: "pending", like_count: 0, comment_count: 0, created_at: ago(11) },
];

export const demoListings: ListingRow[] = [
  { id: "l-1", seller_name: "Mary Achieng (demo)", title: "Scientific calculator", price_kes: 1500, category: "Electronics", status: "pending_review", created_at: ago(2) },
  { id: "l-2", seller_name: "Amina Otieno (demo)", title: "Desk lamp", price_kes: 800, category: "Room essentials", status: "active", created_at: ago(20) },
  { id: "l-3", seller_name: "Brian Mwangi (demo)", title: "Engineering drawing set", price_kes: 2200, category: "Books and supplies", status: "pending_review", created_at: ago(6) },
];

export const demoOpportunities: OpportunityRow[] = [
  { id: "o-1", title: "Frontend developer attachment", organization: "Sample Tech Ltd", type: "attachment", deadline: ahead(12), status: "published", created_at: ago(30) },
  { id: "o-2", title: "Sales assistant internship", organization: "Sample Retail Group", type: "internship", deadline: ahead(21), status: "published", created_at: ago(70) },
  { id: "o-3", title: "Data entry gig", organization: "Campus Services (sample)", type: "gig", deadline: ahead(4), status: "draft", created_at: ago(2) },
];

export const demoReports: ReportRow[] = [
  { id: "r-1", content_type: "listing", content_id: "l-9", reason: "Possible scam", details: "Requests a deposit before viewing the item.", status: "open", created_at: ago(1) },
  { id: "r-2", content_type: "post", content_id: "p-8", reason: "Harassment", details: "Targeted insults in a comment thread.", status: "open", created_at: ago(5) },
  { id: "r-3", content_type: "accommodation", content_id: "a-3", reason: "Fake listing", details: "Photos do not match the address.", status: "open", created_at: ago(13) },
];

export const demoAnnouncements: AnnouncementRow[] = [
  { id: "a-1", title: "Sample exam timetable published", body: "This is fictional sample content for the demo dashboard.", audience: "all", status: "published", published_at: ago(26) },
  { id: "a-2", title: "Library hours extended", body: "Sample announcement. Confirm official hours with the institution.", audience: "students", status: "published", published_at: ago(50) },
];

export const demoDashboard: DashboardData = {
  stats: {
    users: demoProfiles.length,
    posts: demoPosts.length,
    pendingListings: demoListings.filter((l) => l.status === "pending_review").length,
    openReports: demoReports.filter((r) => r.status === "open").length,
    opportunities: demoOpportunities.length,
    applications: 14,
  },
  recentReports: demoReports,
  pendingListings: demoListings.filter((l) => l.status === "pending_review"),
};
