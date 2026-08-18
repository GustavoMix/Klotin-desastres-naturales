package com.gustavomix.desastres.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.Evento
import com.gustavomix.desastres.data.etiquetaTipo
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
    var tipoSeleccionado by remember { mutableStateOf<String?>(null) }

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
                    val tipos = remember(actual.eventos) { actual.eventos.map { it.tipo }.distinct() }
                    val porPestania = filtrarPorPestania(actual.eventos, pestaniaSeleccionada)
                    val filtrados = if (tipoSeleccionado == null) {
                        porPestania
                    } else {
                        porPestania.filter { it.tipo == tipoSeleccionado }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyRow(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            item {
                                FilterChip(
                                    selected = tipoSeleccionado == null,
                                    onClick = { tipoSeleccionado = null },
                                    label = { Text("Todos los tipos") },
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
