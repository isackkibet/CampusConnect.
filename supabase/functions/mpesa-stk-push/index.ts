// M-PESA STK push. The client only receives a checkout request id; payment status
// is only ever set by the Daraja callback (mpesa-callback), never by this app.
// Required secrets: MPESA_CONSUMER_KEY, MPESA_CONSUMER_SECRET, MPESA_SHORTCODE,
// MPESA_PASSKEY, MPESA_CALLBACK_URL, MPESA_ENV (sandbox|production).
import { createClient } from "jsr:@supabase/supabase-js@2";

const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  if (req.method !== "POST") return json({ error: "Method not allowed" }, 405);

  const authHeader = req.headers.get("Authorization") ?? "";
  if (!authHeader.startsWith("Bearer ")) return json({ error: "Authentication required" }, 401);

  const url = Deno.env.get("SUPABASE_URL")!;
  const anon = Deno.env.get("SUPABASE_ANON_KEY")!;
  const service = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
  const consumerKey = Deno.env.get("MPESA_CONSUMER_KEY");
  const consumerSecret = Deno.env.get("MPESA_CONSUMER_SECRET");
  const shortcode = Deno.env.get("MPESA_SHORTCODE");
  const passkey = Deno.env.get("MPESA_PASSKEY");
  const callbackUrl = Deno.env.get("MPESA_CALLBACK_URL");
  if (!consumerKey || !consumerSecret || !shortcode || !passkey || !callbackUrl) {
    return json({ error: "M-PESA is not configured (missing server secrets)" }, 503);
  }

  const userClient = createClient(url, anon, {
    global: { headers: { Authorization: authHeader } },
    auth: { persistSession: false },
  });
  const { data: userData, error: userError } = await userClient.auth.getUser();
  const user = userData?.user;
  if (userError || !user) return json({ error: "Invalid session" }, 401);

  let body: { amountKes?: unknown; phone?: unknown; purpose?: unknown; reference?: unknown };
  try {
    body = await req.json();
  } catch {
    return json({ error: "Invalid JSON body" }, 400);
  }

  const amount = Number(body.amountKes);
  const phoneRaw = typeof body.phone === "string" ? body.phone : "";
  const purpose = typeof body.purpose === "string" ? body.purpose.trim() : "";
  const reference = typeof body.reference === "string" ? body.reference.trim() : "";
  if (!Number.isInteger(amount) || amount < 1 || amount > 150000) {
    return json({ error: "amountKes must be an integer between 1 and 150000" }, 400);
  }
  if (!/^CC-[A-Z0-9]{8}$/.test(reference)) {
    return json({ error: "reference must look like CC-XXXXXXXX" }, 400);
  }
  if (purpose.length < 3 || purpose.length > 160) {
    return json({ error: "purpose must be 3-160 characters" }, 400);
  }
  const phone = normalizePhone(phoneRaw);
  if (!phone) return json({ error: "Enter a valid Safaricom number (07.. or 2547..)" }, 400);

  const admin = createClient(url, service, { auth: { persistSession: false } });
  const { data: profile } = await admin
    .from("profiles").select("suspended").eq("id", user.id).maybeSingle();
  if (profile?.suspended) return json({ error: "Account suspended" }, 403);

  const { data: existing } = await admin
    .from("payments").select("id,status").eq("reference", reference).maybeSingle();
  if (existing) return json({ error: "That payment reference already exists" }, 409);

  const { data: payment, error: insertError } = await admin
    .from("payments")
    .insert({
      user_id: user.id,
      reference,
      amount_kes: amount,
      purpose,
      status: "pending",
    })
    .select("id")
    .single();
  if (insertError || !payment) return json({ error: "Could not start payment" }, 500);

  try {
    const base = Deno.env.get("MPESA_ENV") === "production"
      ? "https://api.safaricom.co.ke"
      : "https://sandbox.safaricom.co.ke";
    const basic = btoa(`${consumerKey}:${consumerSecret}`);
    const tokenResponse = await fetch(
      `${base}/oauth/v1/generate?grant_type=client_credentials`,
      { headers: { Authorization: `Basic ${basic}` } },
    );
    if (!tokenResponse.ok) throw new Error(`OAuth failed (${tokenResponse.status})`);
    const tokenPayload = await tokenResponse.json();
    const accessToken = tokenPayload.access_token;
    if (!accessToken) throw new Error("No access token");

    const timestamp = darajaTimestamp(new Date());
    const password = btoa(`${shortcode}${passkey}${timestamp}`);
    const stkResponse = await fetch(`${base}/mpesa/stkpush/v1/processrequest`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        BusinessShortCode: shortcode,
        Password: password,
        Timestamp: timestamp,
        TransactionType: "CustomerPayBillOnline",
        Amount: amount,
        PartyA: phone,
        PartyB: shortcode,
        PhoneNumber: phone,
        CallBackURL: callbackUrl,
        AccountReference: reference,
        TransactionDesc: purpose,
      }),
    });

    const stk = await stkResponse.json();
    if (!stkResponse.ok || !stk.CheckoutRequestID) {
      const message = stk?.errorMessage ?? stk?.ResponseDescription ?? "STK push rejected";
      await admin.from("payments").update({ status: "failed" }).eq("id", payment.id);
      return json({ error: String(message).slice(0, 200) }, 502);
    }

    await admin.from("payments").update({
      provider_reference: stk.CheckoutRequestID,
    }).eq("id", payment.id);

    return json({
      checkoutRequestId: stk.CheckoutRequestID,
      message: stk.CustomerMessage ?? "STK push sent. Enter your M-PESA PIN.",
    });
  } catch (error) {
    await admin.from("payments").update({ status: "failed" }).eq("id", payment.id);
    console.error("STK error", error);
    return json({ error: "Could not reach M-PESA. Try again later." }, 502);
  }
});

function normalizePhone(input: string): string | null {
  const digits = input.replace(/[^0-9]/g, "");
  if (/^254(7|1)\d{8}$/.test(digits)) return digits;
  if (/^0(7|1)\d{8}$/.test(digits)) return `254${digits.slice(1)}`;
  if (/^(7|1)\d{8}$/.test(digits)) return `254${digits}`;
  return null;
}

function darajaTimestamp(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${date.getFullYear()}${pad(date.getMonth() + 1)}${pad(date.getDate())}` +
    `${pad(date.getHours())}${pad(date.getMinutes())}${pad(date.getSeconds())}`;
}

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...cors, "Content-Type": "application/json" },
  });
}
