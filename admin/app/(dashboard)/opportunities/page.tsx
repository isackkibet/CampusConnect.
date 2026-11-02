import { getOpportunities } from "@/lib/data";
import { formatDate, statusTone } from "@/lib/format";
import { ActionButtons, type AdminAction } from "@/components/ActionButtons";

export const dynamic = "force-dynamic";

export default async function OpportunitiesPage() {
  const opportunities = await getOpportunities();

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Opportunities</h1>
        <p className="text-sm text-slate-500">
          Publish verified opportunities and close expired listings. Only staff can publish.
        </p>
      </div>

      <div className="space-y-3">
        {opportunities.map((opportunity) => {
          const actions: AdminAction[] = [];
          if (opportunity.status !== "published") {
            actions.push({
              label: "Publish",
              action: "moderate_opportunity",
              payload: { status: "published" },
            });
          }
          if (opportunity.status === "published") {
            actions.push({
              label: "Close",
              action: "moderate_opportunity",
              payload: { status: "closed" },
              tone: "neutral",
            });
          }

          return (
            <article key={opportunity.id} className="card flex flex-wrap items-start justify-between gap-3 p-4">
              <div className="min-w-0 flex-1">
                <div className="flex flex-wrap items-center gap-2">
                  <p className="text-sm font-semibold text-slate-900">{opportunity.title}</p>
                  <span className={`badge ${statusTone(opportunity.status)}`}>{opportunity.status}</span>
                  <span className="badge badge-teal">{opportunity.type}</span>
                </div>
                <p className="mt-1 text-xs text-slate-500">
                  {opportunity.organization} · deadline {formatDate(opportunity.deadline)} · posted{" "}
                  {formatDate(opportunity.created_at)}
                </p>
              </div>
              {actions.length > 0 ? <ActionButtons id={opportunity.id} actions={actions} /> : null}
            </article>
          );
        })}
        {opportunities.length === 0 ? (
          <div className="card p-8 text-center text-sm text-slate-500">No opportunities found.</div>
        ) : null}
      </div>
    </div>
  );
}
