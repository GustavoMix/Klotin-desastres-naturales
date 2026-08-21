package com.gustavomix.desastres.data

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Todo se muestra en hora de Bolivia, sin importar la zona horaria del teléfono. */
val ZONA_BOLIVIA: ZoneId = ZoneId.of("America/La_Paz")

private val ESPANIOL: Locale = Locale.forLanguageTag("es")

private val FORMATO_CORTO = DateTimeFormatter.ofPattern("d 'de' MMMM, HH:mm", ESPANIOL)
    .withZone(ZONA_BOLIVIA)

private val FORMATO_LARGO = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, HH:mm", ESPANIOL)
    .withZone(ZONA_BOLIVIA)

private fun instanteDe(fechaIso: String?): Instant? =
    fechaIso?.let { runCatching { Instant.parse(it) }.getOrNull() }

/** "17 de agosto, 03:17" — en hora boliviana. */
fun horaBolivia(fechaIso: String?): String? =
    instanteDe(fechaIso)?.let { FORMATO_CORTO.format(it) }

/** "17 de agosto de 2026, 03:17" — en hora boliviana. */
fun horaBoliviaLarga(fechaIso: String?): String? =
    instanteDe(fechaIso)?.let { FORMATO_LARGO.format(it) }

/** "Hace 4 días", "Hace 5 horas", "Recién". */
fun tiempoRelativo(fechaIso: String?): String? {
    val instante = instanteDe(fechaIso) ?: return null
    val transcurrido = Duration.between(instante, Instant.now())
    if (transcurrido.isNegative) return "Recién"

    val minutos = transcurrido.toMinutes()
    val horas = transcurrido.toHours()
    val dias = transcurrido.toDays()
    return when {
        minutos < 2 -> "Recién"
        minutos < 60 -> "Hace $minutos minutos"
        horas < 24 -> "Hace $horas ${plural(horas, "hora", "horas")}"
        dias < 7 -> "Hace $dias ${plural(dias, "día", "días")}"
        dias < 31 -> (dias / 7).let { "Hace $it ${plural(it, "semana", "semanas")}" }
        else -> (dias / 30).let { "Hace $it ${plural(it, "mes", "meses")}" }
    }
}

private fun plural(cantidad: Long, singular: String, plural: String): String =
    if (cantidad == 1L) singular else plural

private val DIRECCIONES = mapOf(
    "N" to "norte",
    "S" to "sur",
    "E" to "este",
    "W" to "oeste",
    "NE" to "noreste",
    "NW" to "noroeste",
    "SE" to "sureste",
    "SW" to "suroeste",
    "NNE" to "norte-noreste",
    "ENE" to "este-noreste",
    "ESE" to "este-sureste",
    "SSE" to "sur-sureste",
    "SSW" to "sur-suroeste",
    "WSW" to "oeste-suroeste",
    "WNW" to "oeste-noroeste",
    "NNW" to "norte-noroeste",
)

private val PATRON_LUGAR_USGS = Regex(
    """^(\d+(?:\.\d+)?\s*km)\s+([NSEW]{1,3})\s+of\s+(.+)$""",
    RegexOption.IGNORE_CASE,
)

/** Se queda solo con el nombre del sitio: "51 km NNE of Chase, Alaska" → "Chase, Alaska". */
fun lugarCorto(lugarCrudo: String?): String? {
    if (lugarCrudo == null) return null
    val coincidencia = PATRON_LUGAR_USGS.find(lugarCrudo) ?: return lugarCrudo.ifBlank { null }
    return coincidencia.destructured.component3().ifBlank { null }
}

/** "a 51 km al norte-noreste" — la distancia sola, cuando el título ya dice el lugar. */
fun distanciaLegible(lugarCrudo: String?): String? {
    if (lugarCrudo == null) return null
    val coincidencia = PATRON_LUGAR_USGS.find(lugarCrudo) ?: return null
    val (distancia, direccion, _) = coincidencia.destructured
    return "a $distancia al ${direccionLegible(direccion)}"
}

private fun direccionLegible(direccion: String): String =
    DIRECCIONES[direccion.uppercase()] ?: direccion.lowercase()

/** Nombre del país en español, sacado del código ISO que manda el scraper ("ID" → "Indonesia"). */
fun paisLegible(evento: Evento): String? {
    val codigo = evento.paises.firstOrNull()?.takeIf { it.length == 2 }
    if (codigo != null) {
        val nombre = runCatching {
            Locale.Builder().setRegion(codigo).build().getDisplayCountry(ESPANIOL)
        }.getOrNull()
        if (!nombre.isNullOrBlank() && !nombre.equals(codigo, ignoreCase = true)) return nombre
    }
    return evento.pais
}

/**
 * El país solo cuando agrega algo: si el lugar ya dice "Ende, Indonesia" no hace falta
 * repetir "Indonesia" abajo.
 */
fun paisSiAporta(evento: Evento): String? {
    val pais = paisLegible(evento) ?: return null
    val lugar = lugarCorto(evento.lugar) ?: return pais
    return if (lugar.contains(pais, ignoreCase = true)) null else pais
}

/** Frase clara y en español: "Terremoto en Chase, Alaska". */
fun tituloEvento(evento: Evento): String {
    val tipo = etiquetaTipo(evento.tipo)
    val lugar = lugarCorto(evento.lugar) ?: evento.pais
    return if (lugar.isNullOrBlank()) tipo else "$tipo en $lugar"
}
