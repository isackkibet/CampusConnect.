// Daraja STK callback. Configure MPESA_CALLBACK_URL as
// https://<project>.supabase.co/functions/v1/mpesa-callback?secret=<MPESA_CALLBACK_SECRET>
// This function must run with gateway JWT verification disabled (see config.toml)
// because Safaricom does not send a Supabase JWT. The shared secret is mandatory.
import { createClient } from "jsr:@supabase/supabase-js@2";

Deno.serve(async (req) => {
  if (req.method !== "POST") return new Response("Method not allowed", { status: 405 });

  const expected = Deno.env.get("MPESA_CALLBACK_SECRET");
  const supplied = new URL(req.url).searchParams.get("secret");
  if (!expected || !supplied || !timingSafeEqual(expected, supplied)) {
    console.error("Rejected M-PESA callback with bad secret");
    return new Response("Forbidden", { status: 403 });
  }

  let payload: {
    Body?: {
      stkCallback?: {
        MerchantRequestID?: string;
        CheckoutRequestID?: string;
        ResultCode?: number;
        ResultDesc?: string;
      };
    };
  };
  try {
    payload = await req.json();
  } catch {
    return new Response("Bad request", { status: 400 });
  }

  const callback = payload?.Body?.stkCallback;
  const checkoutId = callback?.CheckoutRequestID;
  if (!checkoutId) {
    console.error("Callback missing CheckoutRequestID");
    return ok();
  }

  const status = callback?.ResultCode === 0 ? "success" : "failed";
  const admin = createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
    { auth: { persistSession: false } },
  );

  const { error } = await admin
    .from("payments")
    .update({
      status,
      provider_reference: checkoutId,
    })
    .eq("provider_reference", checkoutId);

  if (error) {
    console.error("Could not update payment", error.message);
    return new Response("Database error", { status: 500 });
  }

  console.log("Payment callback", checkoutId, status, callback?.ResultDesc ?? "");
  return ok();
});

function ok() {
  return new Response(JSON.stringify({ ResultCode: 0, ResultDesc: "Accepted" }), {
    status: 200,
    headers: { "Content-Type": "application/json" },
  });
}

function timingSafeEqual(a: string, b: string): boolean {
  if (a.length !== b.length) return false;
  let diff = 0;
  for (let i = 0; i < a.length; i++) diff |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return diff === 0;
}
