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
)

data class Feed(
    val generado: String,
    val total: Int,
    val eventos: List<Evento>,
)
