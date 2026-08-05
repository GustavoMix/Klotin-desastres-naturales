import type { Adapter, NormalizedDisaster } from "../types.ts";

// ReliefWeb pide un "appname" propio para poder filtrarte por User-Agent
// en sus logs; no hace falta API key. https://reliefweb.int/help/api
const APPNAME = Deno.env.get("RELIEFWEB_APPNAME") ?? "klotin-desastres-naturales";
const LIMIT = 50;

const DISASTER_TYPE_MAP: Record<string, NormalizedDisaster["type"]> = {
  Flood: "flood",
  "Flash Flood": "flood",
  "Forest Fire": "wildfire",
  "Wild Fire": "wildfire",
  Earthquake: "earthquake",
  "Volcanic Eruption": "volcano",
  Drought: "drought",
  "Tropical Cyclone": "cyclone",
};

interface ReliefWebReport {
  id: number;
  fields: {
    title: string;
    url: string;
    date?: { created?: string };
    body?: string;
    disaster_type?: { name: string }[];
  };
}

export const reliefwebAdapter: Adapter = {
  source: "reliefweb",
  async fetch(): Promise<NormalizedDisaster[]> {
    const url = new URL("https://api.reliefweb.int/v1/reports");
    url.searchParams.set("appname", APPNAME);
    url.searchParams.set("filter[field]", "country.iso3");
    url.searchParams.set("filter[value]", "BOL");
    url.searchParams.set("sort[]", "date:desc");
    url.searchParams.set("limit", String(LIMIT));
    url.searchParams.set("fields[include][]", "title");
    url.searchParams.append("fields[include][]", "url");
    url.searchParams.append("fields[include][]", "date");
    url.searchParams.append("fields[include][]", "body");
    url.searchParams.append("fields[include][]", "disaster_type");

    const res = await fetch(url);
    if (!res.ok) {
      throw new Error(`ReliefWeb respondió ${res.status}`);
    }
    const body = await res.json();
    const reports: ReliefWebReport[] = body.data ?? [];

    return reports.map((r) => {
      const disasterName = r.fields.disaster_type?.[0]?.name;
      return {
        source: "reliefweb",
        external_id: String(r.id),
        type: (disasterName && DISASTER_TYPE_MAP[disasterName]) || "other",
        title: r.fields.title,
        description: r.fields.body?.slice(0, 2000) ?? null,
        severity: null,
        country: "BO",
        department: null,
        latitude: null,
        longitude: null,
        event_date: r.fields.date?.created
          ? new Date(r.fields.date.created).toISOString()
          : null,
        url: r.fields.url,
        raw_data: r,
      };
    });
  },
};
