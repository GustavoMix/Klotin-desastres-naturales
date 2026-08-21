package com.gustavomix.desastres.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.etiquetaTipo

@Composable
fun PantallaNoticias(
    viewModel: EventosViewModel,
    alVerAlerta: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val estado by viewModel.estado.collectAsState()
    var tipoSeleccionado by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            Text(
                "Noticias",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextoPrimario,
            )
            Text(
                "Todo lo que reportaron el USGS y el GDACS, del más nuevo al más viejo.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Box(Modifier.weight(1f)) {
            when (val actual = estado) {
                is EstadoEventos.Cargando -> EstadoCargando()
                is EstadoEventos.Error -> EstadoError(actual.mensaje, { viewModel.cargar() })
                is EstadoEventos.Listo -> {
                    val tipos = remember(actual.eventos) { actual.eventos.map { it.tipo }.distinct() }
                    val filtrados = remember(actual.eventos, tipoSeleccionado) {
                        if (tipoSeleccionado == null) actual.eventos
                        else actual.eventos.filter { it.tipo == tipoSeleccionado }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyRow(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            item {
                                FilterChip(
                                    selected = tipoSeleccionado == null,
                                    onClick = { tipoSeleccionado = null },
                                    label = { Text("Todo") },
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
                            EstadoVacio("No hay noticias de este tipo por ahora.")
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
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
