package com.gustavomix.desastres.data

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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

/** Convierte "51 km NNE of Chase, Alaska" en "51 km al norte-noreste de Chase, Alaska". */
fun lugarLegible(lugarCrudo: String?): String? {
    if (lugarCrudo == null) return null
    val coincidencia = PATRON_LUGAR_USGS.find(lugarCrudo) ?: return lugarCrudo
    val (distancia, direccion, lugar) = coincidencia.destructured
    val direccionLegible = DIRECCIONES[direccion.uppercase()] ?: direccion.lowercase()
    return "$distancia al $direccionLegible de $lugar"
}

private val FORMATO_FECHA = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale("es", "ES"))
    .withZone(ZoneId.systemDefault())

/** Convierte "2026-08-17T07:17:16Z" en "17 ago 2026, 07:17". */
fun fechaLegible(fechaIso: String?): String? {
    if (fechaIso == null) return null
    val instante = runCatching { Instant.parse(fechaIso) }.getOrNull() ?: return null
    return FORMATO_FECHA.format(instante)
}
