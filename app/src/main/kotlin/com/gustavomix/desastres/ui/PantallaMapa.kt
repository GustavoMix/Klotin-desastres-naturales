package com.gustavomix.desastres.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.Evento
import com.gustavomix.desastres.data.Severidad
import com.gustavomix.desastres.data.etiquetaTipo
import com.gustavomix.desastres.data.severidadDe

@Composable
fun PantallaMapa(viewModel: EventosViewModel, modifier: Modifier = Modifier) {
    val estado by viewModel.estado.collectAsState()

    when (val actual = estado) {
        is EstadoEventos.Cargando -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is EstadoEventos.Error -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se pudo cargar: ${actual.mensaje}")
        }
        is EstadoEventos.Listo -> ContenidoMapa(actual.eventos, modifier)
    }
}

@Composable
private fun ContenidoMapa(eventos: List<Evento>, modifier: Modifier = Modifier) {
    val conCoordenadas = remember(eventos) {
        eventos.filter { it.latitud != null && it.longitud != null }
    }
    val tipos = remember(eventos) { eventos.map { it.tipo }.distinct() }
    var tipoSeleccionado by remember { mutableStateOf<String?>(null) }

    val filtrados = if (tipoSeleccionado == null) {
        conCoordenadas
    } else {
        conCoordenadas.filter { it.tipo == tipoSeleccionado }
    }

    Column(modifier = modifier.fillMaxSize().padding(12.dp)) {
        Text(
            "Mapa de eventos (beta) — ubicación aproximada, sin capas geográficas todavía",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        LazyRow(modifier = Modifier.padding(bottom = 12.dp)) {
            item {
                FilterChip(
                    selected = tipoSeleccionado == null,
                    onClick = { tipoSeleccionado = null },
                    label = { Text("Todos") },
                    modifier = Modifier.padding(end = 6.dp),
                )
            }
            items(tipos) { tipo ->
                FilterChip(
                    selected = tipoSeleccionado == tipo,
                    onClick = { tipoSeleccionado = tipo },
                    label = { Text(etiquetaTipo(tipo)) },
                    modifier = Modifier.padding(end = 6.dp),
                )
            }
        }

        MapaAbstracto(filtrados, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))

        Text("Leyenda", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp))
        Row {
            Severidad.entries.forEach { severidad ->
                Row(modifier = Modifier.padding(end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    PuntoSeveridad(severidad)
                    Text(
                        etiquetaSeveridad(severidad),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MapaAbstracto(eventos: List<Evento>, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(SuperficieOscura),
    ) {
        if (eventos.isEmpty()) {
            Text(
                "Sin coordenadas para este filtro",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Center),
            )
            return@Box
        }

        val latitudes = eventos.mapNotNull { it.latitud }
        val longitudes = eventos.mapNotNull { it.longitud }
        val latMin = latitudes.min()
        val latMax = latitudes.max()
        val lonMin = longitudes.min()
        val lonMax = longitudes.max()
        val rangoLat = (latMax - latMin).takeIf { it > 0.0 } ?: 1.0
        val rangoLon = (lonMax - lonMin).takeIf { it > 0.0 } ?: 1.0

        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            eventos.forEach { evento ->
                val lat = evento.latitud ?: return@forEach
                val lon = evento.longitud ?: return@forEach
                val x = ((lon - lonMin) / rangoLon).toFloat() * size.width
                // Latitud crece hacia el norte; Y crece hacia abajo en el canvas.
                val y = (1f - ((lat - latMin) / rangoLat).toFloat()) * size.height
                drawCircle(
                    color = colorDeSeveridad(severidadDe(evento)),
                    radius = 6f,
                    center = androidx.compose.ui.geometry.Offset(x, y),
                )
            }
        }
    }
}
