"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

export function AnnouncementForm() {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [audience, setAudience] = useState("all");
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setSaving(true);
    setMessage(null);
    try {
      const response = await fetch("/api/admin/action", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          action: "create_announcement",
          payload: { title, body, audience },
        }),
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.error ?? "Could not publish");
      setMessage(data.demo ? "Demo: announcement simulated, not saved." : "Announcement published.");
      if (!data.demo) {
        setTitle("");
        setBody("");
      }
      router.refresh();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Could not publish");
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={submit} className="card space-y-3 p-5">
      <h2 className="text-base font-semibold text-slate-900">Publish announcement</h2>
      <div>
        <label className="mb-1 block text-xs font-semibold text-slate-600" htmlFor="title">
          Title
        </label>
        <input
          id="title"
          className="field"
          value={title}
          onChange={(event) => setTitle(event.target.value)}
          maxLength={140}
          required
          placeholder="e.g. Library hours this week"
        />
      </div>
      <div>
        <label className="mb-1 block text-xs font-semibold text-slate-600" htmlFor="body">
          Message
        </label>
        <textarea
          id="body"
          className="field min-h-28"
          value={body}
          onChange={(event) => setBody(event.target.value)}
          maxLength={4000}
          required
          placeholder="Write a short, factual announcement."
        />
      </div>
      <div>
        <label className="mb-1 block text-xs font-semibold text-slate-600" htmlFor="audience">
          Audience
        </label>
        <select
          id="audience"
          className="field"
          value={audience}
          onChange={(event) => setAudience(event.target.value)}
        >
          <option value="all">Everyone</option>
          <option value="students">Students</option>
          <option value="staff">Staff</option>
        </select>
      </div>
      <button type="submit" disabled={saving} className="btn btn-primary">
        {saving ? "Publishing…" : "Publish announcement"}
      </button>
      {message ? <p className="text-xs text-slate-600">{message}</p> : null}
    </form>
  );
}
