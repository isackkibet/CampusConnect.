import Link from "next/link";
import { getDashboardData } from "@/lib/data";
import { formatDate, formatKes, statusTone } from "@/lib/format";
import { ActionButtons } from "@/components/ActionButtons";

export const dynamic = "force-dynamic";

export default async function DashboardPage() {
  const data = await getDashboardData();

  const cards = [
    { label: "Users", value: data.stats.users, href: "/users" },
    { label: "Community posts", value: data.stats.posts, href: "/posts" },
    { label: "Listings awaiting review", value: data.stats.pendingListings, href: "/marketplace" },
    { label: "Open reports", value: data.stats.openReports, href: "/reports" },
    { label: "Opportunities", value: data.stats.opportunities, href: "/opportunities" },
    { label: "Applications", value: data.stats.applications, href: "/opportunities" },
  ];

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Overview</h1>
        <p className="text-sm text-slate-500">
          Moderation queue, safety reports and platform activity.
        </p>
      </div>

      <div className="grid grid-cols-2 gap-3 lg:grid-cols-3 xl:grid-cols-6">
        {cards.map((card) => (
          <Link key={card.label} href={card.href} className="card p-4 transition hover:border-teal-500">
            <p className="text-2xl font-bold text-slate-900">{card.value}</p>
            <p className="mt-1 text-xs font-medium text-slate-500">{card.label}</p>
          </Link>
        ))}
      </div>

      <div className="grid gap-5 xl:grid-cols-2">
        <section className="card overflow-hidden">
          <div className="flex items-center justify-between border-b border-slate-200 px-4 py-3">
            <h2 className="text-sm font-semibold text-slate-900">Listings awaiting review</h2>
            <Link href="/marketplace" className="text-xs font-semibold text-teal-600">
              View all
            </Link>
          </div>
          {data.pendingListings.length === 0 ? (
            <p className="p-4 text-sm text-slate-500">No listings are waiting for review.</p>
          ) : (
            <ul className="divide-y divide-slate-100">
              {data.pendingListings.map((listing) => (
                <li key={listing.id} className="flex flex-wrap items-center gap-3 p-4">
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-semibold text-slate-900">{listing.title}</p>
                    <p className="text-xs text-slate-500">
                      {listing.seller_name} · {formatKes(listing.price_kes)} · {formatDate(listing.created_at)}
                    </p>
                  </div>
                  <ActionButtons
                    id={listing.id}
                    actions={[
                      {
                        label: "Approve",
                        action: "moderate_listing",
                        payload: { status: "active" },
                      },
                      {
                        label: "Reject",
                        action: "moderate_listing",
                        payload: { status: "rejected" },
                        tone: "danger",
                      },
                    ]}
                  />
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="card overflow-hidden">
          <div className="flex items-center justify-between border-b border-slate-200 px-4 py-3">
            <h2 className="text-sm font-semibold text-slate-900">Recent reports</h2>
            <Link href="/reports" className="text-xs font-semibold text-teal-600">
              View all
            </Link>
          </div>
          {data.recentReports.length === 0 ? (
            <p className="p-4 text-sm text-slate-500">No open reports.</p>
          ) : (
            <ul className="divide-y divide-slate-100">
              {data.recentReports.map((report) => (
                <li key={report.id} className="flex flex-wrap items-center gap-3 p-4">
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center gap-2">
                      <span className={`badge ${statusTone(report.status)}`}>{report.status}</span>
                      <p className="text-sm font-semibold text-slate-900">{report.reason}</p>
                    </div>
                    <p className="truncate text-xs text-slate-500">
                      {report.content_type} · {report.content_id} · {formatDate(report.created_at)}
                    </p>
                  </div>
                  <ActionButtons
                    id={report.id}
                    actions={[
                      {
                        label: "Resolve",
                        action: "resolve_report",
                        payload: { status: "resolved" },
                      },
                      {
                        label: "Dismiss",
                        action: "resolve_report",
                        payload: { status: "dismissed" },
                        tone: "neutral",
                      },
                    ]}
                  />
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}
