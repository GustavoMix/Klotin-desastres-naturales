import type { Adapter, NormalizedDisaster } from "../types.ts";

// Bounding box de Bolivia (con margen) para no traer sismos de otros países.
const BBOX = {
  minlatitude: -23.0,
  maxlatitude: -9.5,
  minlongitude: -69.7,
  maxlongitude: -57.5,
};

const LOOKBACK_DAYS = Number(Deno.env.get("USGS_LOOKBACK_DAYS") ?? "35");

interface UsgsFeature {
  id: string;
  properties: {
    mag: number | null;
    place: string | null;
    time: number;
    url: string;
    title: string;
  };
  geometry: {
    coordinates: [number, number, number];
  };
}

export const usgsAdapter: Adapter = {
  source: "usgs",
  async fetch(): Promise<NormalizedDisaster[]> {
    const starttime = new Date(Date.now() - LOOKBACK_DAYS * 86_400_000)
      .toISOString()
      .slice(0, 10);

    const url = new URL("https://earthquake.usgs.gov/fdsnws/event/1/query");
    url.searchParams.set("format", "geojson");
    url.searchParams.set("starttime", starttime);
    url.searchParams.set("minlatitude", String(BBOX.minlatitude));
    url.searchParams.set("maxlatitude", String(BBOX.maxlatitude));
    url.searchParams.set("minlongitude", String(BBOX.minlongitude));
    url.searchParams.set("maxlongitude", String(BBOX.maxlongitude));
    url.searchParams.set("orderby", "time");

    const res = await fetch(url);
    if (!res.ok) {
      throw new Error(`USGS respondió ${res.status}`);
    }
    const body = await res.json();
    const features: UsgsFeature[] = body.features ?? [];

    return features.map((f) => ({
      source: "usgs",
      external_id: f.id,
      type: "earthquake",
      title: f.properties.title ?? `Sismo M${f.properties.mag ?? "?"}`,
      description: f.properties.place,
      severity: f.properties.mag != null ? `M${f.properties.mag}` : null,
      country: "BO",
      department: null,
      latitude: f.geometry.coordinates[1] ?? null,
      longitude: f.geometry.coordinates[0] ?? null,
      event_date: new Date(f.properties.time).toISOString(),
      url: f.properties.url,
      raw_data: f,
    }));
  },
};
