import { getStaffSession } from "@/lib/auth";
import { getUsers } from "@/lib/data";
import { formatDate } from "@/lib/format";
import { ActionButtons, type AdminAction } from "@/components/ActionButtons";

export const dynamic = "force-dynamic";

export default async function UsersPage() {
  const [users, staff] = await Promise.all([getUsers(), getStaffSession()]);

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Users</h1>
        <p className="text-sm text-slate-500">
          Verify students, grant staff roles and suspend accounts. You cannot change your own access.
        </p>
      </div>

      <div className="card overflow-x-auto">
        <table className="min-w-full text-sm">
          <thead className="table-head">
            <tr>
              <th className="px-4 py-3">Member</th>
              <th className="px-4 py-3">Institution</th>
              <th className="px-4 py-3">Role</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Joined</th>
              <th className="px-4 py-3">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {users.map((user) => {
              const actions: AdminAction[] = [
                {
                  label: user.verified ? "Unverify" : "Verify",
                  action: "set_verified",
                  payload: { verified: !user.verified },
                  tone: "neutral",
                },
              ];
              if (user.id !== staff?.id) {
                actions.push({
                  label: user.suspended ? "Restore" : "Suspend",
                  action: "set_suspended",
                  payload: { suspended: !user.suspended },
                  tone: user.suspended ? "neutral" : "danger",
                  confirm: user.suspended ? undefined : "Suspend this account? They will lose access.",
                });
                if (staff?.role === "admin") {
                  if (user.role === "student") {
                    actions.push({
                      label: "Make moderator",
                      action: "set_role",
                      payload: { role: "moderator" },
                    });
                  } else {
                    actions.push({
                      label: "Make student",
                      action: "set_role",
                      payload: { role: "student" },
                      tone: "neutral",
                    });
                  }
                  if (user.role === "moderator") {
                    actions.push({
                      label: "Make admin",
                      action: "set_role",
                      payload: { role: "admin" },
                      tone: "neutral",
                      confirm: "Grant full admin access to this account?",
                    });
                  }
                }
              }

              return (
                <tr key={user.id} className="align-top">
                  <td className="px-4 py-3">
                    <p className="font-semibold text-slate-900">{user.full_name || "Unnamed student"}</p>
                    <p className="text-xs text-slate-500">{user.email}</p>
                  </td>
                  <td className="px-4 py-3 text-slate-600">
                    <p>{user.institution_name || "—"}</p>
                    <p className="text-xs text-slate-500">
                      {user.department || "No department"} · Year {user.year_of_study}
                    </p>
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`badge ${
                        user.role === "admin"
                          ? "badge-red"
                          : user.role === "moderator"
                            ? "badge-teal"
                            : "badge-slate"
                      }`}
                    >
                      {user.role}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex flex-col gap-1">
                      <span className={`badge ${user.verified ? "badge-green" : "badge-amber"}`}>
                        {user.verified ? "verified" : "unverified"}
                      </span>
                      {user.suspended ? <span className="badge badge-red">suspended</span> : null}
                    </div>
                  </td>
                  <td className="px-4 py-3 text-xs text-slate-500">{formatDate(user.created_at)}</td>
                  <td className="px-4 py-3">
                    <ActionButtons id={user.id} actions={actions} />
                  </td>
                </tr>
              );
            })}
            {users.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-slate-500">
                  No users found.
                </td>
              </tr>
            ) : null}
          </tbody>
        </table>
      </div>
    </div>
  );
}
