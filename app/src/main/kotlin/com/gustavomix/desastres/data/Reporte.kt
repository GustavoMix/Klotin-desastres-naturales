package com.gustavomix.desastres.data

data class Reporte(
    val id: String,
    val tipo: String,
    val ubicacion: String,
    val descripcion: String,
    val fechaCreacion: Long,
)
