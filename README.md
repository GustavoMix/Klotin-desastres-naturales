# Klotin - Desastres Naturales (Bolivia)

Pipeline que junta datos de desastres naturales en Bolivia desde varias
fuentes y los guarda en Supabase. El front todavía no existe: el foco de
esta primera etapa es dejar el cron andando y validado.

## Fuentes (MVP)

Arrancamos con las 4 fuentes que tienen API pública y no requieren scraping
de HTML (más fáciles de mantener):

| Fuente | Tipo de evento | Necesita API key |
| --- | --- | --- |
| [USGS](https://earthquake.usgs.gov/fdsnws/event/1/) | Sismos | No |
| [GDACS](https://www.gdacs.org/) | Sismos, inundaciones, incendios, volcanes, sequías, ciclones | No |
| [ReliefWeb](https://reliefweb.int/help/api) | Reportes humanitarios/desastres (texto) | No (solo `appname`) |
| [NASA FIRMS](https://firms.modaps.eosdis.nasa.gov/api/area/) | Focos de calor / incendios (satelital) | Sí, gratis |

Pendientes para una siguiente vuelta (necesitan scraping de HTML, más
frágil, así que los sumamos una vez que el pipeline base esté probado):
SENAMHI (`senamhi.gob.bo`, alertas en `bolres.senamhi.gob.bo/alertas/`),
VIDECI/SINAGER (`defensacivil.gob.bo`), Copernicus EMS (activaciones para
Bolivia), Global Volcanism Program (Smithsonian), Gaceta Oficial (decretos
de desastre nacional/departamental), entre otras.

## Setup

1. Creá un proyecto en [supabase.com](https://supabase.com) (si todavía no
   tenés uno).
2. Instalá la Supabase CLI y logueate:
   ```bash
   npm install -g supabase
   supabase login
   supabase link --project-ref <tu-project-ref>
   ```
3. Corré la migración para crear las tablas:
   ```bash
   supabase db push
   ```
4. Sacá una `MAP_KEY` gratis de NASA FIRMS en
   https://firms.modaps.eosdis.nasa.gov/api/area/ (te la mandan por mail).
5. Definí un secreto random para proteger el endpoint del cron (por ejemplo
   `openssl rand -hex 32`).
6. Configurá los secrets de la función:
   ```bash
   supabase secrets set FIRMS_MAP_KEY=xxxxx
   supabase secrets set CRON_SECRET=xxxxx
   supabase secrets set RELIEFWEB_APPNAME=klotin-desastres-naturales
   ```
   (`SUPABASE_URL` y `SUPABASE_SERVICE_ROLE_KEY` ya están disponibles
   automáticamente dentro de las Edge Functions, no hace falta setearlas.)
7. Deployá la función:
   ```bash
   supabase functions deploy scrape-disasters
   ```
8. Probala a mano antes de programarla:
   ```bash
   curl -X POST "https://<tu-project-ref>.supabase.co/functions/v1/scrape-disasters" \
     -H "x-cron-secret: xxxxx"
   ```
   Revisá la tabla `scrape_logs` en el dashboard para ver qué insertó cada
   fuente (y si alguna tiró error).

## Programar el cron

Supabase corre cron jobs a nivel de Postgres con `pg_cron` + `pg_net`
(HTTP desde SQL). En el SQL Editor del dashboard:

```sql
create extension if not exists pg_cron;
create extension if not exists pg_net;

select cron.schedule(
  'scrape-disasters-hourly',
  '0 * * * *', -- cada hora, en punto
  $$
  select net.http_post(
    url := 'https://<tu-project-ref>.supabase.co/functions/v1/scrape-disasters',
    headers := jsonb_build_object('x-cron-secret', 'xxxxx'),
    timeout_milliseconds := 30000
  );
  $$
);
```

Alternativa más simple si no querés tocar SQL: Dashboard → Edge Functions →
`scrape-disasters` → **Cron**, y programarlo ahí directamente (misma idea,
UI en vez de `pg_cron`).

## Agregar una fuente nueva

Cada fuente es un adapter en `supabase/functions/scrape-disasters/adapters/`
que expone `{ source, fetch() }` y devuelve un array de `NormalizedDisaster`
(ver `types.ts`). Se suma a la lista `ADAPTERS` en `index.ts` y listo — el
upsert y el logging son genéricos, no hay que tocar nada más.
