package com.gustavomix.desastres.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Ruta(val ruta: String) {
    data object Inicio : Ruta("inicio")
    data object Alertas : Ruta("alertas?tipo={tipo}") {
        const val BASE = "alertas"
        fun conTipo(tipo: String) = "alertas?tipo=$tipo"
    }
    data object Mapa : Ruta("mapa")
    data object Noticias : Ruta("noticias")
    data object Recursos : Ruta("recursos")
    data object Reportar : Ruta("reportar")
    data object Mas : Ruta("mas")
    data object AlertaDetalle : Ruta("alerta/{id}") {
        fun con(id: String) = "alerta/$id"
    }
}

data class DestinoBarra(val ruta: String, val etiqueta: String, val icono: ImageVector)

val destinosBarraInferior = listOf(
    DestinoBarra(Ruta.Inicio.ruta, "Inicio", Icons.Filled.Home),
    DestinoBarra(Ruta.Mapa.ruta, "Mapa", Icons.Filled.Map),
    DestinoBarra(Ruta.Noticias.ruta, "Noticias", Icons.Filled.Newspaper),
    DestinoBarra(Ruta.Recursos.ruta, "Recursos", Icons.Filled.MenuBook),
    DestinoBarra(Ruta.Mas.ruta, "Más", Icons.Filled.MoreHoriz),
)
