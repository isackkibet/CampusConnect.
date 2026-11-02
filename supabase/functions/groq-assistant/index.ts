// Secure Groq assistant proxy. Secrets stay server-side (Supabase function env).
// Deploy: supabase functions deploy groq-assistant
import { createClient } from "jsr:@supabase/supabase-js@2";

const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

const SYSTEM_PROMPT = `You are the CampusConnect student guide for a Kenyan tertiary institution.
You help with using the app: community, messaging, marketplace, opportunities, accommodation,
events, campus map and account questions. Be concise, friendly and practical.
Never invent official campus policies, fees, exam dates, deadlines or contacts. If asked about
official information, tell the student to confirm with their institution. Never request passwords,
M-PESA PINs or payment secrets. Do not give medical, legal or financial advice beyond general guidance.
Sample data in the app is fictional.`;

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  if (req.method !== "POST") return json({ error: "Method not allowed" }, 405);

  const authHeader = req.headers.get("Authorization") ?? "";
  if (!authHeader.startsWith("Bearer ")) return json({ error: "Authentication required" }, 401);

  const url = Deno.env.get("SUPABASE_URL")!;
  const anon = Deno.env.get("SUPABASE_ANON_KEY")!;
  const service = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
  const groqKey = Deno.env.get("GROQ_API_KEY");
  if (!groqKey) return json({ error: "Assistant is not configured (GROQ_API_KEY missing)" }, 503);

  const userClient = createClient(url, anon, {
    global: { headers: { Authorization: authHeader } },
    auth: { persistSession: false },
  });
  const { data: userData, error: userError } = await userClient.auth.getUser();
  const user = userData?.user;
  if (userError || !user) return json({ error: "Invalid session" }, 401);

  const admin = createClient(url, service, { auth: { persistSession: false } });

  const { data: profile } = await admin
    .from("profiles")
    .select("suspended")
    .eq("id", user.id)
    .maybeSingle();
  if (profile?.suspended) return json({ error: "Account suspended" }, 403);

  const oneMinuteAgo = new Date(Date.now() - 60_000).toISOString();
  const { count } = await admin
    .from("ai_usage")
    .select("id", { count: "exact", head: true })
    .eq("user_id", user.id)
    .gte("created_at", oneMinuteAgo);
  if ((count ?? 0) >= 10) return json({ error: "Too many requests. Try again in a minute." }, 429);

  let body: { messages?: unknown; institutionId?: unknown };
  try {
    body = await req.json();
  } catch {
    return json({ error: "Invalid JSON body" }, 400);
  }

  if (!Array.isArray(body.messages) || body.messages.length === 0) {
    return json({ error: "messages are required" }, 400);
  }
  const history = body.messages.slice(-12).map((item) => {
    const message = item as { role?: unknown; content?: unknown };
    const role = message.role === "assistant" ? "assistant" : "user";
    const content = typeof message.content === "string" ? message.content.slice(0, 4000) : "";
    return { role, content };
  }).filter((m) => m.content.trim().length > 0);
  if (history.length === 0) return json({ error: "messages are empty" }, 400);

  await admin.from("ai_usage").insert({ user_id: user.id });

  const response = await fetch("https://api.groq.com/openai/v1/chat/completions", {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${groqKey}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      model: Deno.env.get("GROQ_MODEL") ?? "llama-3.3-70b-versatile",
      temperature: 0.4,
      max_tokens: 700,
      messages: [
        { role: "system", content: SYSTEM_PROMPT },
        ...history,
      ],
    }),
  });

  if (!response.ok) {
    const detail = await response.text();
    console.error("Groq error", response.status, detail.slice(0, 500));
    return json({ error: "Assistant provider error" }, 502);
  }

  const payload = await response.json();
  const reply = payload?.choices?.[0]?.message?.content;
  if (typeof reply !== "string" || !reply.trim()) {
    return json({ error: "Assistant returned an empty response" }, 502);
  }
  return json({ reply: reply.trim() });
});

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...cors, "Content-Type": "application/json" },
  });
}
