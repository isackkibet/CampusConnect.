import { getAnnouncements } from "@/lib/data";
import { formatDate, statusTone } from "@/lib/format";
import { AnnouncementForm } from "@/components/AnnouncementForm";

export const dynamic = "force-dynamic";

export default async function AnnouncementsPage() {
  const announcements = await getAnnouncements();

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Announcements</h1>
        <p className="text-sm text-slate-500">
          Publish short, factual updates. Avoid inventing official dates, fees or policies.
        </p>
      </div>

      <div className="grid gap-5 xl:grid-cols-2">
        <AnnouncementForm />
        <section className="card overflow-hidden">
          <div className="border-b border-slate-200 px-4 py-3">
            <h2 className="text-sm font-semibold text-slate-900">Recent announcements</h2>
          </div>
          <ul className="divide-y divide-slate-100">
            {announcements.map((announcement) => (
              <li key={announcement.id} className="p-4">
                <div className="flex flex-wrap items-center gap-2">
                  <p className="text-sm font-semibold text-slate-900">{announcement.title}</p>
                  <span className={`badge ${statusTone(announcement.status)}`}>
                    {announcement.status}
                  </span>
                  <span className="badge badge-slate">{announcement.audience}</span>
                </div>
                <p className="mt-1 text-sm text-slate-700">{announcement.body}</p>
                <p className="mt-1 text-xs text-slate-500">{formatDate(announcement.published_at)}</p>
              </li>
            ))}
            {announcements.length === 0 ? (
              <li className="p-8 text-center text-sm text-slate-500">No announcements yet.</li>
            ) : null}
          </ul>
        </section>
      </div>
    </div>
  );
}
