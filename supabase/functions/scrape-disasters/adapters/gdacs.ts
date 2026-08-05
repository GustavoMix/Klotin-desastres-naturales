import type { Adapter, NormalizedDisaster } from "../types.ts";

// GDACS expone los ~100 eventos activos/recientes más recientes en este
// endpoint (sin filtro de país en la URL); filtramos Bolivia del lado del
// cliente por iso3/nombre de país. Si en el futuro GDACS cambia la forma
// de la respuesta, este es el primer lugar para revisar.
const GDACS_EVENTS_URL =
  "https://www.gdacs.org/gdacsapi/api/events/geteventlist/EVENTS";

const EVENT_TYPE_MAP: Record<string, NormalizedDisaster["type"]> = {
  EQ: "earthquake",
  FL: "flood",
  WF: "wildfire",
  VO: "volcano",
  DR: "drought",
  TC: "cyclone",
};

interface GdacsFeature {
  properties: {
    eventid: number;
    episodeid?: number;
    eventtype: string;
    eventname?: string;
    name?: string;
    country?: string;
    iso3?: string;
    fromdate: string;
    alertlevel?: string;
    url?: { report?: string; details?: string };
    htmldescription?: string;
  };
  geometry: {
    coordinates: [number, number];
  };
}

function isBolivia(props: GdacsFeature["properties"]): boolean {
  const country = `${props.country ?? ""}`.toLowerCase();
  return props.iso3 === "BOL" || country.includes("bolivia");
}

export const gdacsAdapter: Adapter = {
  source: "gdacs",
  async fetch(): Promise<NormalizedDisaster[]> {
    const res = await fetch(GDACS_EVENTS_URL);
    if (!res.ok) {
      throw new Error(`GDACS respondió ${res.status}`);
    }
    const body = await res.json();
    const features: GdacsFeature[] = body.features ?? [];

    return features
      .filter((f) => isBolivia(f.properties))
      .map((f) => {
        const p = f.properties;
        return {
          source: "gdacs",
          external_id: `${p.eventid}-${p.episodeid ?? 0}`,
          type: EVENT_TYPE_MAP[p.eventtype] ?? "other",
          title: p.eventname ?? p.name ?? `Evento GDACS ${p.eventtype}`,
          description: p.htmldescription ?? null,
          severity: p.alertlevel ?? null,
          country: "BO",
          department: null,
          latitude: f.geometry?.coordinates?.[1] ?? null,
          longitude: f.geometry?.coordinates?.[0] ?? null,
          event_date: p.fromdate ? new Date(p.fromdate).toISOString() : null,
          url: p.url?.report ?? p.url?.details ?? null,
          raw_data: f,
        };
      });
  },
};
