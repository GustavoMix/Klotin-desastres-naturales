package com.gustavomix.desastres.data

import java.net.URLEncoder
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * De dónde salen las fotos de los eventos.
 *
 * La foto es el mosaico satelital de NASA Worldview del día del evento, recortado
 * a su zona. Es un servicio público sin API key y la URL es pura aritmética sobre
 * la fecha y las coordenadas que el evento ya trae, así que el feed no la manda
 * evento por evento: manda una plantilla y la app la completa. Con 1.357 eventos,
 * mandarla repetida serían cientos de kilobytes de texto idéntico en una descarga
 * que se hace con datos móviles.
 *
 * Los valores de acá abajo son el respaldo para cuando el feed no trae el bloque
 * `media` —feeds anteriores a la versión 2—, para que la app tenga fotos desde el
 * primer arranque. Cuando el feed lo trae, manda el feed: así se puede cambiar de
 * capa satelital o de proveedor desde el cron, sin publicar una app nueva.
 */
data class ConfiguracionMedia(
    val plantillaSatelite: String = PLANTILLA_SATELITE,
    val capa: String = CAPA_SATELITE,
    val credito: String = CREDITO_SATELITE,
    val gradosPorTipo: Map<String, Double> = GRADOS_POR_TIPO,
    val gradosPorDefecto: Double = GRADOS_POR_DEFECTO,
    val diasTimelapse: Int = DIAS_TIMELAPSE,
    val plantillaBusquedaVideos: String = PLANTILLA_BUSQUEDA_VIDEOS,
) {
    companion object {
        const val CAPA_SATELITE = "MODIS_Terra_CorrectedReflectance_TrueColor"
        const val CREDITO_SATELITE = "NASA Worldview (MODIS/Terra)"

        const val PLANTILLA_SATELITE =
            "https://wvs.earthdata.nasa.gov/api/v1/snapshot" +
                "?REQUEST=GetSnapshot" +
                "&LAYERS={capa}" +
                "&CRS=EPSG:4326" +
                "&TIME={fecha}" +
                "&BBOX={sur},{oeste},{norte},{este}" +
                "&FORMAT=image/jpeg" +
                "&WIDTH={ancho}" +
                "&HEIGHT={alto}"

        const val PLANTILLA_BUSQUEDA_VIDEOS =
            "https://www.youtube.com/results?search_query={consulta}"

        /**
         * Lado del recuadro, en grados. Un sismo se ve en su valle; un ciclón ocupa
         * medio mar y con el recuadro de un sismo se lo pierde de vista.
         */
        val GRADOS_POR_TIPO = mapOf(
            "sismo" to 6.0,
            "volcan" to 5.0,
            "incendio" to 4.0,
            "inundacion" to 8.0,
            "ciclon" to 16.0,
            "sequia" to 20.0,
        )
        const val GRADOS_POR_DEFECTO = 8.0
        const val DIAS_TIMELAPSE = 7
    }
}

/** Imágenes que publica la propia fuente del evento. Hoy solo las manda GDACS. */
data class MediaEvento(
    val icono: String? = null,
    val mapa: String? = null,
    val recursos: List<ImagenFuente> = emptyList(),
)

data class ImagenFuente(val url: String, val titulo: String?)

/** ¿Se le puede sacar foto? Hace falta posición y día. */
fun tieneFoto(evento: Evento): Boolean =
    evento.latitud != null && evento.longitud != null && diaDe(evento.fechaEvento) != null

private fun diaDe(fechaIso: String?): LocalDate? {
    val texto = fechaIso?.takeIf { it.length >= 10 }?.substring(0, 10) ?: return null
    return runCatching { LocalDate.parse(texto) }.getOrNull()
}

/**
 * Recuadro (sur, oeste, norte, este) centrado en el evento, si el mundo deja.
 *
 * Cerca de los polos o del antimeridiano no entra centrado. En vez de recortarlo
 * —que devolvería una imagen achatada, o un error si el ancho diera cero— se lo
 * **corre** hacia adentro conservando el tamaño pedido: la foto sigue siendo
 * cuadrada y el evento sigue estando adentro, aunque no justo en el medio.
 */
fun recuadroDe(evento: Evento, configuracion: ConfiguracionMedia): DoubleArray? {
    val latitud = evento.latitud ?: return null
    val longitud = evento.longitud ?: return null
    val grados = maxOf(
        configuracion.gradosPorTipo[evento.tipo] ?: configuracion.gradosPorDefecto,
        0.01,
    )
    val (sur, norte) = ventana(latitud, grados, -90.0, 90.0)
    val (oeste, este) = ventana(longitud, grados, -180.0, 180.0)
    return doubleArrayOf(redondear(sur), redondear(oeste), redondear(norte), redondear(este))
}

private fun ventana(centro: Double, grados: Double, minimo: Double, maximo: Double): Pair<Double, Double> {
    val ancho = minOf(grados, maximo - minimo)
    var inicio = centro - ancho / 2.0
    if (inicio < minimo) inicio = minimo else if (inicio + ancho > maximo) inicio = maximo - ancho
    return inicio to (inicio + ancho)
}

// Cuatro decimales son ~11 m: de sobra para encuadrar, y evita que la URL
// arrastre el ruido binario del double.
private fun redondear(valor: Double): Double = Math.round(valor * 10000.0) / 10000.0

/**
 * Foto satelital del área del evento. `fecha` permite pedir otro día del
 * timelapse; por defecto, el del evento.
 */
fun urlSatelite(
    evento: Evento,
    configuracion: ConfiguracionMedia,
    fecha: LocalDate? = null,
    ancho: Int = 512,
    alto: Int = 512,
): String? {
    val recuadro = recuadroDe(evento, configuracion) ?: return null
    val dia = fecha ?: diaDe(evento.fechaEvento) ?: return null
    return configuracion.plantillaSatelite
        .replace("{capa}", configuracion.capa)
        .replace("{fecha}", dia.toString())
        // El orden es el que espera EPSG:4326 — sur, oeste, norte, este.
        // Invertirlo no da error: devuelve mar vacío, que es peor.
        .replace("{sur}", numero(recuadro[0]))
        .replace("{oeste}", numero(recuadro[1]))
        .replace("{norte}", numero(recuadro[2]))
        .replace("{este}", numero(recuadro[3]))
        .replace("{ancho}", ancho.toString())
        .replace("{alto}", alto.toString())
}

private fun numero(valor: Double): String =
    if (valor == valor.toLong().toDouble()) valor.toLong().toString()
    else valor.toString()

/**
 * Los días del timelapse: el del evento y los siguientes, que es cuando se ve
 * crecer un incendio o avanzar un ciclón.
 *
 * Nunca días futuros: el mosaico de mañana todavía no existe y volvería un
 * rectángulo negro, que parece un error de la app.
 */
fun diasTimelapse(evento: Evento, configuracion: ConfiguracionMedia): List<LocalDate> {
    val inicio = diaDe(evento.fechaEvento) ?: return emptyList()
    val hoy = LocalDate.now(ZoneOffset.UTC)
    return (0 until configuracion.diasTimelapse)
        .map { inicio.plusDays(it.toLong()) }
        .takeWhile { !it.isAfter(hoy) }
}

/**
 * Búsqueda de video del evento en YouTube.
 *
 * Es una **búsqueda**, no un video incrustado, y la interfaz lo dice así: no hay
 * ninguna fuente pública que publique video por evento, y presentarlo como si lo
 * fuera sería mentirle a alguien que está buscando saber qué pasó.
 */
fun urlBusquedaVideos(evento: Evento, configuracion: ConfiguracionMedia): String? {
    val consulta = listOfNotNull(tituloEvento(evento), paisLegible(evento))
        .joinToString(" ")
        .trim()
    if (consulta.isBlank()) return null
    return configuracion.plantillaBusquedaVideos
        .replace("{consulta}", URLEncoder.encode(consulta, "UTF-8"))
}

/** Todas las imágenes propias de la fuente, para la galería del detalle. */
fun imagenesDeLaFuente(evento: Evento): List<ImagenFuente> = buildList {
    evento.media?.mapa?.let { add(ImagenFuente(it, "Mapa de la fuente")) }
    evento.media?.recursos?.let { addAll(it) }
}
