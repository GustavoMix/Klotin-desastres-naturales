package com.gustavomix.desastres.data

data class Evento(
    val id: String,
    val fuente: String,
    val tipo: String,
    val titulo: String,
    val lugar: String?,
    val pais: String?,
    val paises: List<String>,
    val fechaEvento: String?,
    val magnitud: Double?,
    val unidadMagnitud: String?,
    val nivelAlerta: String?,
    val url: String?,
    val latitud: Double?,
    val longitud: Double?,
    val profundidadKm: Double?,
    /** Imágenes que publica la fuente. Null en casi todos: solo GDACS las manda. */
    val media: MediaEvento? = null,
)

data class Feed(
    val generado: String,
    val total: Int,
    val eventos: List<Evento>,
    /** Plantillas para armar las fotos. El feed manda; si no viene, se usan las de la app. */
    val media: ConfiguracionMedia = ConfiguracionMedia(),
)
