package com.gustavomix.desastres.data

import java.util.Locale

enum class Severidad { ROJA, NARANJA, AMARILLA, VERDE }

fun severidadDe(evento: Evento): Severidad {
    when (evento.nivelAlerta?.lowercase()) {
        "red", "rojo" -> return Severidad.ROJA
        "orange", "naranja" -> return Severidad.NARANJA
        "green", "verde" -> return Severidad.VERDE
    }

    return when (evento.tipo) {
        "sismo" -> when {
            (evento.magnitud ?: 0.0) >= 6.0 -> Severidad.ROJA
            (evento.magnitud ?: 0.0) >= 4.5 -> Severidad.NARANJA
            (evento.magnitud ?: 0.0) >= 3.0 -> Severidad.AMARILLA
            else -> Severidad.VERDE
        }
        "incendio" -> when (hectareasQuemadas(evento) ?: 0.0) {
            in 1000.0..Double.MAX_VALUE -> Severidad.ROJA
            in 200.0..1000.0 -> Severidad.NARANJA
            else -> Severidad.AMARILLA
        }
        else -> Severidad.AMARILLA
    }
}

private const val HECTAREAS_POR_ACRE = 0.404686

/**
 * El área quemada, siempre en hectáreas.
 *
 * GDACS la informa en hectáreas y EONET en acres. Sin convertir, un incendio de
 * 900 acres (364 ha) se leería como más grave que uno de 500 ha, que es más del
 * doble de grande. Es el mismo tipo de evento y el mismo campo `magnitud`: la
 * unidad es lo único que los distingue.
 */
fun hectareasQuemadas(evento: Evento): Double? {
    val magnitud = evento.magnitud ?: return null
    val unidad = evento.unidadMagnitud?.lowercase()?.trim()
    return if (unidad == "acres" || unidad == "acre") magnitud * HECTAREAS_POR_ACRE else magnitud
}

/** Qué tan grave es, en palabras que se entienden sin saber de escalas. */
fun explicacionSeveridad(severidad: Severidad): String = when (severidad) {
    Severidad.ROJA -> "Puede causar daños graves"
    Severidad.NARANJA -> "Se sintió fuerte, puede causar daños"
    Severidad.AMARILLA -> "Moderado, poco probable que cause daños"
    Severidad.VERDE -> "Leve, casi no se siente"
}

fun etiquetaTipo(tipo: String): String = when (tipo) {
    "sismo" -> "Terremoto"
    "incendio" -> "Incendio forestal"
    "inundacion" -> "Inundación"
    "sequia" -> "Sequía"
    "ciclon" -> "Ciclón"
    "derrumbe" -> "Derrumbe"
    "volcan" -> "Volcán"
    "otro" -> "Otro evento"
    else -> tipo.replaceFirstChar { it.uppercase() }
}

/** "Magnitud 5.2" para sismos, "1200 ha" para el resto. Null si la fuente no la manda. */
fun magnitudTexto(evento: Evento): String? {
    val magnitud = evento.magnitud ?: return null
    if (evento.tipo == "sismo") return "Magnitud ${formatearNumero(magnitud)}"
    // Los incendios se muestran siempre en hectáreas, vengan como vengan: dos
    // incendios uno al lado del otro con unidades distintas no se pueden comparar.
    if (evento.tipo == "incendio") {
        val hectareas = hectareasQuemadas(evento) ?: return null
        return "${formatearNumero(hectareas)} ha quemadas"
    }
    val unidad = evento.unidadMagnitud
    return if (unidad.isNullOrBlank()) formatearNumero(magnitud) else "${formatearNumero(magnitud)} $unidad"
}

/** Versión corta para la esquina de la tarjeta: "M 5.2". */
fun magnitudCorta(evento: Evento): String? {
    val magnitud = evento.magnitud ?: return null
    if (evento.tipo == "sismo") return "M ${formatearNumero(magnitud)}"
    if (evento.tipo == "incendio") {
        return hectareasQuemadas(evento)?.let { "${formatearNumero(it)} ha" }
    }
    return formatearNumero(magnitud)
}

private fun formatearNumero(valor: Double): String =
    if (valor == valor.toLong().toDouble()) valor.toLong().toString()
    else "%.1f".format(Locale.US, valor)
