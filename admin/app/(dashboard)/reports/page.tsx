import { getReports } from "@/lib/data";
import { formatDate, statusTone } from "@/lib/format";
import { ActionButtons } from "@/components/ActionButtons";

export const dynamic = "force-dynamic";

export default async function ReportsPage() {
  const reports = await getReports();
  const open = reports.filter((report) => report.status === "open");
  const closed = reports.filter((report) => report.status !== "open");

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Safety reports</h1>
        <p className="text-sm text-slate-500">
          Triage reports of scams, harassment and fake listings. Investigate before acting.
        </p>
      </div>

      <section className="card overflow-hidden">
        <div className="border-b border-slate-200 px-4 py-3">
          <h2 className="text-sm font-semibold text-slate-900">Open ({open.length})</h2>
        </div>
        <ul className="divide-y divide-slate-100">
          {open.map((report) => (
            <li key={report.id} className="flex flex-wrap items-start gap-3 p-4">
              <div className="min-w-0 flex-1">
                <div className="flex flex-wrap items-center gap-2">
                  <span className={`badge ${statusTone(report.status)}`}>{report.status}</span>
                  <p className="text-sm font-semibold text-slate-900">{report.reason}</p>
                </div>
                <p className="mt-1 text-xs text-slate-500">
                  {report.content_type} · {report.content_id} · {formatDate(report.created_at)}
                </p>
                {report.details ? <p className="mt-2 text-sm text-slate-700">{report.details}</p> : null}
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
          {open.length === 0 ? (
            <li className="p-8 text-center text-sm text-slate-500">No open reports. Good work.</li>
          ) : null}
        </ul>
      </section>

      <section className="card overflow-hidden">
        <div className="border-b border-slate-200 px-4 py-3">
          <h2 className="text-sm font-semibold text-slate-900">Closed ({closed.length})</h2>
        </div>
        <ul className="divide-y divide-slate-100">
          {closed.map((report) => (
            <li key={report.id} className="flex flex-wrap items-center gap-2 p-4">
              <span className={`badge ${statusTone(report.status)}`}>{report.status}</span>
              <p className="text-sm text-slate-700">{report.reason}</p>
              <span className="text-xs text-slate-500">
                {report.content_type} · {formatDate(report.created_at)}
              </span>
            </li>
          ))}
          {closed.length === 0 ? (
            <li className="p-8 text-center text-sm text-slate-500">No closed reports yet.</li>
          ) : null}
        </ul>
      </section>
    </div>
  );
}
