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
