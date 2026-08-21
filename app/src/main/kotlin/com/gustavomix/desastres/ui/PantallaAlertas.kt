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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.Evento
import com.gustavomix.desastres.data.Severidad
import com.gustavomix.desastres.data.etiquetaTipo
import com.gustavomix.desastres.data.severidadDe
import java.time.Duration
import java.time.Instant

private val PESTANIAS = listOf("Todos", "Los más fuertes", "Última semana")

@Composable
fun PantallaAlertas(
    viewModel: EventosViewModel,
    alVerAlerta: (String) -> Unit,
    pestaniaInicial: Int = 0,
    modifier: Modifier = Modifier,
) {
    val estado by viewModel.estado.collectAsState()
    var pestaniaSeleccionada by remember(pestaniaInicial) {
        mutableIntStateOf(pestaniaInicial.coerceIn(PESTANIAS.indices))
    }
    var tipoSeleccionado by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            "Eventos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextoPrimario,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
        )

        TabRow(selectedTabIndex = pestaniaSeleccionada) {
            PESTANIAS.forEachIndexed { indice, titulo ->
                Tab(
                    selected = pestaniaSeleccionada == indice,
                    onClick = { pestaniaSeleccionada = indice },
                    text = { Text(titulo, style = MaterialTheme.typography.labelLarge) },
                )
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (val actual = estado) {
                is EstadoEventos.Cargando -> EstadoCargando()
                is EstadoEventos.Error -> EstadoError(actual.mensaje, { viewModel.cargar() })
                is EstadoEventos.Listo -> {
                    val tipos = remember(actual.eventos) { actual.eventos.map { it.tipo }.distinct() }
                    val porPestania = remember(actual.eventos, pestaniaSeleccionada) {
                        filtrarPorPestania(actual.eventos, pestaniaSeleccionada)
                    }
                    val filtrados = remember(porPestania, tipoSeleccionado) {
                        if (tipoSeleccionado == null) porPestania
                        else porPestania.filter { it.tipo == tipoSeleccionado }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyRow(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
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
                            EstadoVacio("No hay eventos que coincidan con este filtro.")
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 4.dp,
                                    bottom = 96.dp,
                                ),
                            ) {
                                items(filtrados, key = { it.id }) { evento ->
                                    TarjetaEvento(
                                        evento = evento,
                                        onClick = { alVerAlerta(evento.id) },
                                        modifier = Modifier.padding(bottom = 10.dp),
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
    1 -> eventos.filter {
        val severidad = severidadDe(it)
        severidad == Severidad.ROJA || severidad == Severidad.NARANJA
    }
    2 -> eventos.filter { esDeLaUltimaSemana(it.fechaEvento) }
    else -> eventos
}

private fun esDeLaUltimaSemana(fechaIso: String?): Boolean {
    val fecha = fechaIso?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return false
    return Duration.between(fecha, Instant.now()).toDays() <= 7
}
