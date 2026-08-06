# Desastres naturales — app Kotlin

App Android que muestra alertas de desastres naturales a partir de los datos
que publica
[cron-desastres-naturales](https://github.com/GustavoMix/cron-desastres-naturales).

## Estructura

| Módulo | Qué es | Se testea |
|---|---|---|
| **`:core`** | Kotlin/JVM puro: modelo del feed, parseo, filtros y frescura | Sin SDK ni emulador, en segundos |
| **`:app`** | UI en Jetpack Compose | En CI (necesita el SDK de Android) |

La separación no es ceremonia: **toda la lógica que puede fallar vive en
`:core`**, que no depende de Android y por eso se compila y se testea en
cualquier máquina y en CI sin instalar nada. `:app` solo pinta lo que `:core`
decide.

```bash
./gradlew :core:test        # no necesita el SDK de Android
./gradlew :app:assembleDebug  # sí lo necesita
```

`org.gradle.configureondemand` está activado justamente para que lo primero
funcione sin lo segundo: si el build de raíz declarara el plugin de Android,
Gradle lo resolvería en toda invocación y los tests de `:core` quedarían atados
al SDK.

## Bajar el APK

Todavía no hay releases firmados. El APK **de debug** lo genera CI en cada push:

1. Entrá a la pestaña *Actions* del repo.
2. Abrí el run más reciente del workflow **CI**.
3. Bajá el artifact **`desastres-debug-apk`** (abajo de todo, en *Artifacts*).
4. En el celular, habilitá "instalar apps de origen desconocido" e instalalo.

Es un APK de debug: sirve para probar, no para publicar. Para Play Store hace
falta un `release` firmado con un keystore propio.

## De dónde salen los datos

```
https://raw.githubusercontent.com/GustavoMix/cron-desastres-naturales/claude/scraper-cron-j5z2ny/datos/recientes.json
```

Un scraper corre **una vez por semana** en GitHub Actions (lunes 06:17 UTC),
junta USGS y GDACS, normaliza todo y publica ese JSON. La app solo lo lee.

⚠️ **Dos cosas antes de que esa URL sirva:**

1. `recientes.json` **todavía no existe** en el repo. Aparece recién en la
   próxima corrida del scraper; para no esperar al lunes, disparalo a mano desde
   *Actions → Scraper de desastres → Run workflow*.
2. La rama por defecto de ese repo es `claude/scraper-cron-j5z2ny`, no `main`
   (fue la primera que se empujó a un repo vacío). Si se renombra a `main`, hay
   que actualizar esta URL.

Para producción conviene servirlo por **jsDelivr** en vez de
`raw.githubusercontent.com`, que no es un CDN y tiene rate limits:

```
https://cdn.jsdelivr.net/gh/GustavoMix/cron-desastres-naturales@<rama>/datos/recientes.json
```

Y cachear con ETag desde OkHttp: si el archivo no cambió el servidor responde
`304` y no se baja nada, que con un scraper semanal es casi siempre.

Tres cosas del contrato que conviene no olvidar:

- **Filtrá por `paises`, no por `pais`.** `pais` es texto crudo de la fuente:
  USGS pone estados de EE. UU. ahí (`CA`, `Alaska`) y GDACS mete varios países
  en un solo campo. `paises` son códigos ISO-3166 alfa-2 ya normalizados, y es
  lista porque un ciclón abarca varios países de verdad.
- **`magnitud` no es una escala única.** Son hectáreas quemadas en incendios,
  km/h en ciclones, km² en sequías. Mirá `unidad_magnitud` antes de comparar o
  de ordenar, y nunca apliques un umbral sísmico a otro tipo de evento.
- **Colgá los comentarios de `id_agrupado`, no de `id`.** GDACS republica un
  mismo evento por episodios; `id` los distingue e `id_agrupado` los junta.

El parser ignora las claves que no conoce **a propósito**: el scraper agrega
campos sin coordinarse con la app, y una app ya publicada sigue leyendo feeds
más nuevos durante meses. Si eso se saca, un campo nuevo tumba a todos los
usuarios que no actualizaron.

## Frescura de los datos

Con el cron semanal, los datos pueden tener **hasta 7 días**. Esto no es una app
de tiempo real, y la interfaz tiene que decirlo.

`EvaluadorDeFrescura` compara la marca `generado` del feed contra el reloj y
devuelve `Fresca` / `Vieja` / `Crítica` / `Desconocida`. Cuando `requiereAviso`
es `true`, la pantalla **debe** advertirlo: mostrar información vieja como si
fuera actual, en una app de desastres, es peor que no mostrar nada.

Los umbrales están atados a la cadencia del cron. Si el scraper cambia de
cadencia, hay que moverlos en el constructor de `EvaluadorDeFrescura`.

## `supabase/` — pipeline anterior, sin uso hoy

El directorio `supabase/` tiene un pipeline en TypeScript que scrapea las mismas
fuentes (más ReliefWeb y NASA FIRMS) y guarda en Postgres, **scopeado solo a
Bolivia**. Quedó fuera del camino cuando la app pasó a leer el JSON del scraper
en Python, que es global.

Se conserva como referencia por dos motivos: los adapters de **ReliefWeb** y
**NASA FIRMS**, que el scraper en Python todavía no tiene, y el esquema con RLS,
que va a servir cuando se sumen los comentarios de usuarios — porque eso sí
necesita una base de datos, no un JSON estático.

No está desplegado ni tiene CI.
