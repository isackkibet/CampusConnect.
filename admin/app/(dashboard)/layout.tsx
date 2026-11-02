import Link from "next/link";
import { redirect } from "next/navigation";
import { getStaffSession } from "@/lib/auth";
import { isDemoMode } from "@/lib/config";
import { Sidebar } from "@/components/Sidebar";
import { DemoBanner } from "@/components/DemoBanner";

export default async function DashboardLayout({ children }: { children: React.ReactNode }) {
  const staff = await getStaffSession();
  if (!staff) redirect("/login");

  const navigation = [
    ["/dashboard", "Overview"],
    ["/users", "Users"],
    ["/posts", "Posts"],
    ["/marketplace", "Marketplace"],
    ["/opportunities", "Opportunities"],
    ["/reports", "Reports"],
    ["/announcements", "Announcements"],
  ];

  return (
    <div className="flex min-h-screen bg-slate-100">
      <Sidebar />
      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 bg-white px-4 py-3 lg:px-8">
          <div>
            <p className="text-sm font-semibold text-slate-900">{staff.fullName}</p>
            <p className="text-xs text-slate-500">
              {staff.email} · <span className="capitalize">{staff.role}</span>
              {staff.demo ? " · demo session" : ""}
            </p>
          </div>
          <div className="flex items-center gap-2">
            <span className={`badge ${isDemoMode ? "badge-amber" : "badge-teal"}`}>
              {isDemoMode ? "Demo data" : "Supabase connected"}
            </span>
            <form action="/auth/signout" method="post">
              <button type="submit" className="btn btn-neutral">
                Sign out
              </button>
            </form>
          </div>
        </header>

        <div className="flex gap-2 overflow-x-auto border-b border-slate-200 bg-navy-950 px-4 py-2 lg:hidden">
          {navigation.map(([href, label]) => (
            <Link
              key={href}
              href={href}
              className="whitespace-nowrap rounded-lg px-3 py-1.5 text-xs font-semibold text-slate-200 hover:bg-white/10"
            >
              {label}
            </Link>
          ))}
        </div>

        <main className="flex-1 space-y-5 p-4 lg:p-8">
          {isDemoMode ? <DemoBanner /> : null}
          {children}
        </main>
      </div>
    </div>
  );
}
