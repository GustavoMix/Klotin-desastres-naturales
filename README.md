# Klotin - Desastres Naturales

App Android (Kotlin + Jetpack Compose) que muestra terremotos, incendios e
inundaciones de todo el mundo, con las horas en hora de Bolivia y la foto del
satélite sobre cada zona.

## Estructura

- `app/` — módulo único de la aplicación.
- `app/src/main/assets/mundo.txt` — contornos de los continentes que dibuja el mapa.

## De dónde salen los datos

Los eventos vienen del feed que publica el scraper de
[cron-desastres-naturales](https://github.com/GustavoMix/cron-desastres-naturales),
que junta USGS (terremotos), GDACS (incendios, inundaciones, ciclones) y NASA
EONET (incendios, volcanes, tormentas) una vez por semana. La app lo lee por
jsDelivr, no por `raw.githubusercontent.com`, que tiene límite de peticiones.

El cliente HTTP tiene caché en disco a propósito: como el scraper corre
semanalmente, el archivo es idéntico entre corridas y el CDN responde 304 casi
siempre. Sin caché no hay ETag que valga y cada apertura se baja el feed entero.

## Fotos y timelapse

Cada evento con coordenadas muestra el mosaico satelital de
[NASA Worldview](https://worldview.earthdata.nasa.gov) (MODIS/Terra) recortado a
su zona, el día que ocurrió. Es un servicio público sin API key.

**Las URLs las arma la app, no vienen en el feed.** El feed publica una plantilla
en su bloque `media` y la app la completa con la fecha y las coordenadas que el
evento ya trae. Con ~1.400 eventos, mandar la URL repetida serían cientos de
kilobytes de texto idéntico en una descarga que se hace con datos móviles; y así
el cron puede cambiar de capa satelital o de proveedor sin que nadie tenga que
actualizar el APK. Si el feed no trae el bloque, valen los valores de fábrica de
`ConfiguracionMedia`, así que la app nunca se queda sin fotos.

Dos cosas que hay que respetar al armar el recuadro:

- El BBOX de EPSG:4326 va **`sur,oeste,norte,este`**. Invertirlo no da error:
  devuelve mar vacío, que es peor.
- Cerca de los polos o del antimeridiano el recuadro no entra centrado. Se lo
  **corre** hacia adentro conservando el tamaño en vez de recortarlo: recortado
  saldría una imagen deformada, y si el ancho diera cero, un error.

El timelapse del detalle pide el mismo recuadro en días consecutivos. Nunca días
futuros: el mosaico de mañana todavía no existe y volvería un rectángulo negro
que parece un error de la app.

El botón de videos abre una **búsqueda** en YouTube y así está etiquetado: no
existe ninguna fuente pública que publique video por evento, y presentarlo como
si lo fuera sería mentirle a alguien que quiere saber qué pasó.

## Mapa

El mapa se dibuja con Canvas en proyección equirectangular, a partir de los
contornos de `assets/mundo.txt`. Ese archivo se generó a partir de
[world-atlas](https://github.com/topojson/world-atlas) (ISC), cuyos datos vienen
de [Natural Earth](https://www.naturalearthdata.com) (dominio público),
simplificados para que pesen poco.

Los sismos fuertes laten como anillos que se abren desde el epicentro y los
ciclones como brazos en espiral que giran (`ui/Ondas.kt`). **Solo se animan los
eventos naranja y rojo**: si latieran los ~1.400 puntos del feed, el mapa sería
una pantalla temblando y no se distinguiría ninguno.

## Notificaciones

`avisos/TrabajoDeAvisos.kt` revisa el feed cada 6 horas con WorkManager. Seis y
no quince minutos porque el scraper corre una vez por semana: consultar más
seguido no encuentra nada y gasta batería y datos de alguien.

Arrancan **apagadas** y se prenden desde la pantalla Más, con umbral de gravedad
y filtro "solo Bolivia". Al prenderlas se marca como visto todo lo que ya está en
el feed: si no, el primer chequeo encontraría catorce días de eventos "nuevos" y
dispararía decenas de avisos de golpe, que es la forma más rápida de que alguien
las apague para siempre.

Hay dos canales de notificación, uno para eventos fuertes y otro para el resto,
para que se le pueda bajar el volumen a lo leve sin perder la alerta roja.

## Unidades

`magnitud` no es una escala única: es magnitud en sismos, hectáreas en incendios
de GDACS, **acres** en los de EONET y km/h en ciclones. Los incendios se
convierten siempre a hectáreas antes de mostrarlos o clasificarlos
(`data/Severidad.kt`): sin eso, uno de 900 acres (364 ha) se leería como más
grave que uno de 500 ha.

## Zona horaria

Todo lo que se muestra al usuario está en hora de Bolivia (`America/La_Paz`),
sin importar la zona horaria que tenga configurada el teléfono. Las fechas
también se muestran en relativo ("hace 4 días") porque el scraper corre
semanalmente y así queda claro qué tan viejo es cada dato.

## Desarrollo

```
./gradlew :app:assembleDebug
```

CI compila el APK en cada push y, en los push a `main` o en cualquier corrida
manual del workflow, lo publica en el release `apk-ultimo` con link directo para
bajarlo desde el teléfono.
