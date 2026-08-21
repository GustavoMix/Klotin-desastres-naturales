# Klotin - Desastres Naturales

App Android (Kotlin + Jetpack Compose) que muestra terremotos, incendios e
inundaciones de todo el mundo, con las horas en hora de Bolivia.

## Estructura

- `app/` — módulo único de la aplicación.
- `app/src/main/assets/mundo.txt` — contornos de los continentes que dibuja el mapa.

## De dónde salen los datos

Los eventos vienen del feed que publica el scraper de
[cron-desastres-naturales](https://github.com/GustavoMix/cron-desastres-naturales),
que junta USGS (terremotos) y GDACS (incendios, inundaciones, ciclones) una vez
por semana. La app lo lee por jsDelivr, no por `raw.githubusercontent.com`, que
tiene límite de peticiones.

## Zona horaria

Todo lo que se muestra al usuario está en hora de Bolivia (`America/La_Paz`),
sin importar la zona horaria que tenga configurada el teléfono. Las fechas
también se muestran en relativo ("hace 4 días") porque el scraper corre
semanalmente y así queda claro qué tan viejo es cada dato.

## Mapa

El mapa se dibuja con Canvas en proyección equirectangular, a partir de los
contornos de `assets/mundo.txt`. Ese archivo se generó a partir de
[world-atlas](https://github.com/topojson/world-atlas) (ISC), cuyos datos vienen
de [Natural Earth](https://www.naturalearthdata.com) (dominio público),
simplificados para que pesen poco.

## Desarrollo

```
./gradlew :app:assembleDebug
```
