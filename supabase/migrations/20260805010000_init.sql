-- Esquema inicial: eventos de desastres naturales (arranca con Bolivia,
-- pero el modelo es agnóstico de país para poder sumar otros más adelante).

create table if not exists public.disasters (
  id uuid primary key default gen_random_uuid(),
  source text not null,               -- 'usgs' | 'gdacs' | 'reliefweb' | 'firms' | ...
  external_id text not null,          -- id que usa la fuente, para poder hacer upsert
  type text not null,                 -- 'earthquake' | 'flood' | 'wildfire' | 'volcano' | 'drought' | 'cyclone' | 'other'
  title text not null,
  description text,
  severity text,                     -- magnitud, nivel de alerta (green/orange/red), etc. según la fuente
  country text not null default 'BO',
  department text,
  latitude double precision,
  longitude double precision,
  event_date timestamptz,
  url text,
  raw_data jsonb,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (source, external_id)
);

create index if not exists disasters_event_date_idx on public.disasters (event_date desc);
create index if not exists disasters_type_idx on public.disasters (type);
create index if not exists disasters_country_idx on public.disasters (country);

create table if not exists public.scrape_logs (
  id uuid primary key default gen_random_uuid(),
  source text not null,
  started_at timestamptz not null default now(),
  finished_at timestamptz,
  inserted_count int not null default 0,
  updated_count int not null default 0,
  error text
);

alter table public.disasters enable row level security;
alter table public.scrape_logs enable row level security;

-- Lectura pública (el front va a leer con la anon key); la escritura queda
-- reservada a la service role key que usa el cron.
create policy "disasters_public_read" on public.disasters
  for select using (true);

create policy "scrape_logs_public_read" on public.scrape_logs
  for select using (true);
