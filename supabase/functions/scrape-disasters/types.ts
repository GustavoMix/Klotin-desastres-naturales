export type DisasterType =
  | "earthquake"
  | "flood"
  | "wildfire"
  | "volcano"
  | "drought"
  | "cyclone"
  | "other";

export interface NormalizedDisaster {
  source: string;
  external_id: string;
  type: DisasterType;
  title: string;
  description: string | null;
  severity: string | null;
  country: string;
  department: string | null;
  latitude: number | null;
  longitude: number | null;
  event_date: string | null;
  url: string | null;
  raw_data: unknown;
}

export interface Adapter {
  source: string;
  fetch: () => Promise<NormalizedDisaster[]>;
}
