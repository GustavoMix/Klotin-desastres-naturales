import type { Adapter, NormalizedDisaster } from "../types.ts";

// NASA FIRMS requiere una MAP_KEY gratuita: https://firms.modaps.eosdis.nasa.gov/api/area/
const MAP_KEY = Deno.env.get("FIRMS_MAP_KEY");

// west,south,east,north (Bolivia con margen)
const BBOX = "-69.7,-23.0,-57.5,-9.5";
const DAY_RANGE = 1; // FIRMS solo deja pedir hasta 10 días hacia atrás por request
const DATASET = "VIIRS_SNPP_NRT";

function parseCsv(csv: string): Record<string, string>[] {
  const lines = csv.trim().split("\n");
  if (lines.length <= 1) return [];
  const headers = lines[0].split(",");
  return lines.slice(1).map((line) => {
    const cols = line.split(",");
    return Object.fromEntries(headers.map((h, i) => [h, cols[i]]));
  });
}

export const firmsAdapter: Adapter = {
  source: "firms",
  async fetch(): Promise<NormalizedDisaster[]> {
    if (!MAP_KEY) {
      throw new Error(
        "Falta la env var FIRMS_MAP_KEY (sacala gratis en firms.modaps.eosdis.nasa.gov/api/area/)",
      );
    }

    const url =
      `https://firms.modaps.eosdis.nasa.gov/api/area/csv/${MAP_KEY}/${DATASET}/${BBOX}/${DAY_RANGE}`;
    const res = await fetch(url);
    if (!res.ok) {
      throw new Error(`FIRMS respondió ${res.status}`);
    }
    const csv = await res.text();
    const rows = parseCsv(csv);

    return rows.map((row) => {
      const lat = Number(row.latitude);
      const lon = Number(row.longitude);
      const externalId = `${row.acq_date}-${row.acq_time}-${row.latitude}-${row.longitude}-${row.satellite}`;
      return {
        source: "firms",
        external_id: externalId,
        type: "wildfire",
        title: `Foco de calor (${row.satellite ?? "satélite"})`,
        description:
          `Confianza: ${row.confidence ?? "?"}, FRP: ${row.frp ?? "?"} MW`,
        severity: row.confidence ?? null,
        country: "BO",
        department: null,
        latitude: Number.isFinite(lat) ? lat : null,
        longitude: Number.isFinite(lon) ? lon : null,
        event_date: row.acq_date
          ? new Date(`${row.acq_date}T00:00:00Z`).toISOString()
          : null,
        url: null,
        raw_data: row,
      };
    });
  },
};
