import { getListings } from "@/lib/data";
import { formatDate, formatKes, statusTone } from "@/lib/format";
import { ActionButtons, type AdminAction } from "@/components/ActionButtons";

export const dynamic = "force-dynamic";

export default async function MarketplacePage() {
  const listings = await getListings();

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Marketplace moderation</h1>
        <p className="text-sm text-slate-500">
          Approve genuine listings, reject suspicious ones and mark completed sales.
        </p>
      </div>

      <div className="card overflow-x-auto">
        <table className="min-w-full text-sm">
          <thead className="table-head">
            <tr>
              <th className="px-4 py-3">Listing</th>
              <th className="px-4 py-3">Seller</th>
              <th className="px-4 py-3">Price</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Submitted</th>
              <th className="px-4 py-3">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {listings.map((listing) => {
              const actions: AdminAction[] = [];
              if (listing.status === "pending_review") {
                actions.push({
                  label: "Approve",
                  action: "moderate_listing",
                  payload: { status: "active" },
                });
                actions.push({
                  label: "Reject",
                  action: "moderate_listing",
                  payload: { status: "rejected" },
                  tone: "danger",
                });
              }
              if (listing.status === "active") {
                actions.push({
                  label: "Mark sold",
                  action: "moderate_listing",
                  payload: { status: "sold" },
                  tone: "neutral",
                });
              }

              return (
                <tr key={listing.id}>
                  <td className="px-4 py-3">
                    <p className="font-semibold text-slate-900">{listing.title}</p>
                    <p className="text-xs text-slate-500">{listing.category}</p>
                  </td>
                  <td className="px-4 py-3 text-slate-600">{listing.seller_name}</td>
                  <td className="px-4 py-3 font-medium text-slate-900">{formatKes(listing.price_kes)}</td>
                  <td className="px-4 py-3">
                    <span className={`badge ${statusTone(listing.status)}`}>{listing.status}</span>
                  </td>
                  <td className="px-4 py-3 text-xs text-slate-500">{formatDate(listing.created_at)}</td>
                  <td className="px-4 py-3">
                    {actions.length > 0 ? (
                      <ActionButtons id={listing.id} actions={actions} />
                    ) : (
                      <span className="text-xs text-slate-400">No action</span>
                    )}
                  </td>
                </tr>
              );
            })}
            {listings.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-slate-500">
                  No listings found.
                </td>
              </tr>
            ) : null}
          </tbody>
        </table>
      </div>
    </div>
  );
}
