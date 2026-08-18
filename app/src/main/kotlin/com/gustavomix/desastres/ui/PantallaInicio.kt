package com.gustavomix.desastres.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.Evento
import com.gustavomix.desastres.data.Severidad
import com.gustavomix.desastres.data.fechaLegible
import com.gustavomix.desastres.data.resumenClaro
import com.gustavomix.desastres.data.severidadDe

@Composable
fun PantallaInicio(
    viewModel: EventosViewModel,
    alVerAlerta: (String) -> Unit,
    alVerTodasLasAlertas: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val estado by viewModel.estado.collectAsState()

    when (val actual = estado) {
        is EstadoEventos.Cargando -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is EstadoEventos.Error -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se pudo cargar: ${actual.mensaje}")
        }
        is EstadoEventos.Listo -> ContenidoInicio(actual.eventos, alVerAlerta, alVerTodasLasAlertas, modifier)
    }
}

@Composable
private fun ContenidoInicio(
    eventos: List<Evento>,
    alVerAlerta: (String) -> Unit,
    alVerTodasLasAlertas: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val criticos = eventos
        .filter { severidadDe(it) in setOf(Severidad.ROJA, Severidad.NARANJA) }
        .sortedByDescending { it.fechaEvento ?: "" }
    val recientes = eventos.take(8)

    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            Text(
                "Desastres Naturales",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }

        if (criticos.isNotEmpty()) {
            item {
                val masReciente = criticos.first()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = alVerTodasLasAlertas)
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorRoja),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "${criticos.size} ALERTA${if (criticos.size == 1) "" else "S"} ACTIVA${if (criticos.size == 1) "" else "S"}",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            resumenClaro(masReciente),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        fechaLegible(masReciente.fechaEvento)?.let {
                            Text(it, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 2.dp))
                        }
                        Text(
                            "Toca para ver todas",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Eventos recientes", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Ver todos",
                    style = MaterialTheme.typography.labelLarge,
                    color = AzulAcento,
                    modifier = Modifier.clickable(onClick = alVerTodasLasAlertas),
                )
            }
        }
        items(recientes, key = { it.id }) { evento ->
            TarjetaEvento(
                evento = evento,
                onClick = { alVerAlerta(evento.id) },
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
    }
}
