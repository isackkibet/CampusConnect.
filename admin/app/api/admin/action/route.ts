import { NextResponse } from "next/server";
import { createClient } from "@supabase/supabase-js";
import { getStaffSession } from "@/lib/auth";
import { isDemoMode, supabaseUrl } from "@/lib/config";

type Body = {
  action?: string;
  id?: string;
  payload?: Record<string, unknown>;
};

export async function POST(request: Request) {
  if (request.headers.get("sec-fetch-site") === "cross-site") {
    return json({ error: "Cross-site requests are not allowed" }, 403);
  }

  const staff = await getStaffSession();
  if (!staff) return json({ error: "Staff access required" }, 401);

  let body: Body;
  try {
    body = await request.json();
  } catch {
    return json({ error: "Invalid JSON" }, 400);
  }

  if (!body.action) return json({ error: "action is required" }, 400);

  if (staff.demo || isDemoMode) {
    return json({
      ok: true,
      demo: true,
      message: "Demo mode: the action was simulated and nothing was persisted.",
    });
  }

  const serviceKey = process.env.SUPABASE_SERVICE_ROLE_KEY;
  if (!serviceKey) {
    return json({ error: "SUPABASE_SERVICE_ROLE_KEY is not configured on the server" }, 500);
  }

  const admin = createClient(supabaseUrl, serviceKey, {
    auth: { persistSession: false, autoRefreshToken: false },
  });

  const id = body.id;
  const payload = body.payload ?? {};

  try {
    switch (body.action) {
      case "set_role": {
        if (!id) return json({ error: "id is required" }, 400);
        if (id === staff.id) return json({ error: "You cannot change your own role" }, 400);
        if (staff.role !== "admin") return json({ error: "Only admins can change roles" }, 403);
        const role = String(payload.role ?? "");
        if (!["student", "moderator", "admin"].includes(role)) {
          return json({ error: "Invalid role" }, 400);
        }
        const { error } = await admin.from("profiles").update({ role }).eq("id", id);
        if (error) throw error;
        return json({ ok: true });
      }
      case "set_suspended": {
        if (!id) return json({ error: "id is required" }, 400);
        if (id === staff.id) return json({ error: "You cannot suspend your own account" }, 400);
        const suspended = Boolean(payload.suspended);
        const { error } = await admin.from("profiles").update({ suspended }).eq("id", id);
        if (error) throw error;
        return json({ ok: true });
      }
      case "set_verified": {
        if (!id) return json({ error: "id is required" }, 400);
        const verified = Boolean(payload.verified);
        const { error } = await admin.from("profiles").update({ verified }).eq("id", id);
        if (error) throw error;
        return json({ ok: true });
      }
      case "moderate_post": {
        if (!id) return json({ error: "id is required" }, 400);
        const status = String(payload.status ?? "");
        if (!["published", "hidden", "removed"].includes(status)) {
          return json({ error: "Invalid post status" }, 400);
        }
        const { error } = await admin.from("posts").update({ status }).eq("id", id);
        if (error) throw error;
        return json({ ok: true });
      }
      case "delete_post": {
        if (!id) return json({ error: "id is required" }, 400);
        if (staff.role !== "admin" && staff.role !== "moderator") {
          return json({ error: "Staff access required" }, 403);
        }
        const { error } = await admin.from("posts").delete().eq("id", id);
        if (error) throw error;
        return json({ ok: true });
      }
      case "moderate_listing": {
        if (!id) return json({ error: "id is required" }, 400);
        const status = String(payload.status ?? "");
        if (!["active", "rejected", "sold"].includes(status)) {
          return json({ error: "Invalid listing status" }, 400);
        }
        const { error } = await admin.from("listings").update({ status }).eq("id", id);
        if (error) throw error;
        return json({ ok: true });
      }
      case "moderate_opportunity": {
        if (!id) return json({ error: "id is required" }, 400);
        const status = String(payload.status ?? "");
        if (!["published", "draft", "closed"].includes(status)) {
          return json({ error: "Invalid opportunity status" }, 400);
        }
        const { error } = await admin.from("opportunities").update({ status }).eq("id", id);
        if (error) throw error;
        return json({ ok: true });
      }
      case "resolve_report": {
        if (!id) return json({ error: "id is required" }, 400);
        const status = String(payload.status ?? "");
        if (!["resolved", "dismissed"].includes(status)) {
          return json({ error: "Invalid report status" }, 400);
        }
        const { error } = await admin
          .from("reports")
          .update({ status, resolved_at: new Date().toISOString(), resolved_by: staff.id })
          .eq("id", id);
        if (error) throw error;
        return json({ ok: true });
      }
      case "create_announcement": {
        const title = String(payload.title ?? "").trim();
        const announcementBody = String(payload.body ?? "").trim();
        const audience = String(payload.audience ?? "all");
        if (title.length < 4 || title.length > 140) {
          return json({ error: "Title must be 4-140 characters" }, 400);
        }
        if (announcementBody.length < 10 || announcementBody.length > 4000) {
          return json({ error: "Body must be 10-4000 characters" }, 400);
        }
        if (!["all", "students", "staff"].includes(audience)) {
          return json({ error: "Invalid audience" }, 400);
        }
        const { error } = await admin.from("announcements").insert({
          title,
          body: announcementBody,
          audience,
          status: "published",
          published_by: staff.id,
        });
        if (error) throw error;
        return json({ ok: true });
      }
      default:
        return json({ error: "Unknown action" }, 400);
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : "Action failed";
    console.error("Admin action failed", body.action, message);
    return json({ error: message }, 500);
  }
}

function json(body: unknown, status = 200) {
  return NextResponse.json(body, { status });
}
