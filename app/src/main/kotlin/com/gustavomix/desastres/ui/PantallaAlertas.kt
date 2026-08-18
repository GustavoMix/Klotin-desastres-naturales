package com.gustavomix.desastres.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.Evento
import java.time.Instant
import java.time.temporal.ChronoUnit

private val PESTANIAS = listOf("Todas", "Activas", "Historial")

@Composable
fun PantallaAlertas(
    viewModel: EventosViewModel,
    alVerAlerta: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val estado by viewModel.estado.collectAsState()
    var pestaniaSeleccionada by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pestaniaSeleccionada) {
            PESTANIAS.forEachIndexed { indice, titulo ->
                Tab(
                    selected = pestaniaSeleccionada == indice,
                    onClick = { pestaniaSeleccionada = indice },
                    text = { Text(titulo) },
                )
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (val actual = estado) {
                is EstadoEventos.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is EstadoEventos.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se pudo cargar: ${actual.mensaje}")
                }
                is EstadoEventos.Listo -> {
                    val filtrados = filtrarPorPestania(actual.eventos, pestaniaSeleccionada)
                    if (filtrados.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nada por acá", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(12.dp)) {
                            items(filtrados, key = { it.id }) { evento ->
                                TarjetaEvento(
                                    evento = evento,
                                    onClick = { alVerAlerta(evento.id) },
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun filtrarPorPestania(eventos: List<Evento>, pestania: Int): List<Evento> = when (pestania) {
    1 -> eventos.filter { esReciente(it.fechaEvento) }
    2 -> eventos.filterNot { esReciente(it.fechaEvento) }
    else -> eventos
}

private fun esReciente(fechaIso: String?): Boolean {
    val fecha = fechaIso?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return false
    return ChronoUnit.HOURS.between(fecha, Instant.now()) <= 48
}
