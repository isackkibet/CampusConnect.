"use client";

import { useState, useTransition } from "react";
import { useRouter } from "next/navigation";

export interface AdminAction {
  label: string;
  action: string;
  payload?: Record<string, unknown>;
  tone?: "primary" | "danger" | "neutral";
  confirm?: string;
}

export function ActionButtons({ id, actions }: { id: string; actions: AdminAction[] }) {
  const router = useRouter();
  const [pending, startTransition] = useTransition();
  const [message, setMessage] = useState<string | null>(null);
  const [busyAction, setBusyAction] = useState<string | null>(null);

  async function run(adminAction: AdminAction) {
    if (adminAction.confirm && !window.confirm(adminAction.confirm)) return;
    setBusyAction(adminAction.action);
    setMessage(null);
    try {
      const response = await fetch("/api/admin/action", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          action: adminAction.action,
          id,
          payload: adminAction.payload ?? {},
        }),
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.error ?? "Action failed");
      setMessage(data.demo ? "Demo: simulated only, not saved." : "Saved.");
      startTransition(() => router.refresh());
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Action failed");
    } finally {
      setBusyAction(null);
    }
  }

  return (
    <div className="flex flex-col items-start gap-1">
      <div className="flex flex-wrap gap-1.5">
        {actions.map((adminAction) => (
          <button
            key={`${adminAction.action}-${adminAction.label}`}
            type="button"
            disabled={pending || busyAction !== null}
            onClick={() => run(adminAction)}
            className={`btn ${
              adminAction.tone === "danger"
                ? "btn-danger"
                : adminAction.tone === "neutral"
                  ? "btn-neutral"
                  : "btn-primary"
            }`}
          >
            {busyAction === adminAction.action ? "Working…" : adminAction.label}
          </button>
        ))}
      </div>
      {message ? <span className="text-xs text-slate-500">{message}</span> : null}
    </div>
  );
}
