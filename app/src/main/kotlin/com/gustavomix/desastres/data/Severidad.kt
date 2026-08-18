package com.gustavomix.desastres.data

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

fun etiquetaTipo(tipo: String): String = when (tipo) {
    "sismo" -> "Terremoto"
    "incendio" -> "Incendio forestal"
    "inundacion" -> "Inundación"
    "sequia" -> "Sequía"
    "ciclon" -> "Ciclón"
    "derrumbe" -> "Derrumbe"
    "volcan" -> "Volcán"
    else -> tipo.replaceFirstChar { it.uppercase() }
}

/** Frase corta y clara: "Terremoto · Magnitud 3.1" en vez del título técnico crudo de la fuente. */
fun resumenClaro(evento: Evento): String {
    val tipo = etiquetaTipo(evento.tipo)
    val magnitud = evento.magnitud ?: return tipo
    return "$tipo · Magnitud ${formatearMagnitud(magnitud)}"
}

private fun formatearMagnitud(valor: Double): String =
    "%.1f".format(java.util.Locale.US, valor)
