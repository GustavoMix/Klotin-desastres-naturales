import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import type { Adapter } from "./types.ts";
import { usgsAdapter } from "./adapters/usgs.ts";
import { gdacsAdapter } from "./adapters/gdacs.ts";
import { reliefwebAdapter } from "./adapters/reliefweb.ts";
import { firmsAdapter } from "./adapters/firms.ts";

// Cada fuente nueva se suma acá; el resto del pipeline (upsert + logging)
// no cambia.
const ADAPTERS: Adapter[] = [
  usgsAdapter,
  gdacsAdapter,
  reliefwebAdapter,
  firmsAdapter,
];

const supabase = createClient(
  Deno.env.get("SUPABASE_URL")!,
  Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
);

async function runAdapter(adapter: Adapter) {
  const startedAt = new Date().toISOString();
  try {
    const records = await adapter.fetch();

    if (records.length === 0) {
      await supabase.from("scrape_logs").insert({
        source: adapter.source,
        started_at: startedAt,
        finished_at: new Date().toISOString(),
        inserted_count: 0,
        updated_count: 0,
      });
      return { source: adapter.source, count: 0 };
    }

    const { error } = await supabase
      .from("disasters")
      .upsert(records, { onConflict: "source,external_id" });

    if (error) throw error;

    await supabase.from("scrape_logs").insert({
      source: adapter.source,
      started_at: startedAt,
      finished_at: new Date().toISOString(),
      inserted_count: records.length,
      updated_count: 0,
    });

    return { source: adapter.source, count: records.length };
  } catch (err) {
    const message = err instanceof Error ? err.message : String(err);
    await supabase.from("scrape_logs").insert({
      source: adapter.source,
      started_at: startedAt,
      finished_at: new Date().toISOString(),
      inserted_count: 0,
      updated_count: 0,
      error: message,
    });
    return { source: adapter.source, error: message };
  }
}

const CRON_SECRET = Deno.env.get("CRON_SECRET");

Deno.serve(async (req) => {
  if (CRON_SECRET && req.headers.get("x-cron-secret") !== CRON_SECRET) {
    return new Response("Unauthorized", { status: 401 });
  }

  const results = await Promise.all(ADAPTERS.map(runAdapter));
  return new Response(JSON.stringify({ results }, null, 2), {
    headers: { "Content-Type": "application/json" },
  });
});
