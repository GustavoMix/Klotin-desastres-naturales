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
        "incendio" -> when {
            (evento.magnitud ?: 0.0) >= 1000.0 -> Severidad.ROJA
            (evento.magnitud ?: 0.0) >= 200.0 -> Severidad.NARANJA
            else -> Severidad.AMARILLA
        }
        else -> Severidad.AMARILLA
    }
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

/** "Magnitud 5.2" para sismos, "1.200 ha" para el resto. Null si la fuente no la manda. */
fun magnitudTexto(evento: Evento): String? {
    val magnitud = evento.magnitud ?: return null
    if (evento.tipo == "sismo") return "Magnitud ${formatearNumero(magnitud)}"
    val unidad = evento.unidadMagnitud
    return if (unidad.isNullOrBlank()) formatearNumero(magnitud) else "${formatearNumero(magnitud)} $unidad"
}

/** Versión corta para la esquina de la tarjeta: "M 5.2". */
fun magnitudCorta(evento: Evento): String? {
    val magnitud = evento.magnitud ?: return null
    return if (evento.tipo == "sismo") "M ${formatearNumero(magnitud)}" else formatearNumero(magnitud)
}

private fun formatearNumero(valor: Double): String =
    if (valor == valor.toLong().toDouble()) valor.toLong().toString()
    else "%.1f".format(Locale.US, valor)
