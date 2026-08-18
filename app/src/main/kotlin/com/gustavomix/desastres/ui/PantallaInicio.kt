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
import com.gustavomix.desastres.data.etiquetaTipo
import com.gustavomix.desastres.data.severidadDe
import com.gustavomix.desastres.data.Severidad

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
    val criticos = eventos.filter { severidadDe(it) in setOf(Severidad.ROJA, Severidad.NARANJA) }
    val recientes = eventos.take(8)
    val tipos = eventos.map { it.tipo }.distinct().take(6)

    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            Column {
                Text("Desastres", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Naturales",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AzulAcento,
                )
                Text(
                    "Información que puede salvar vidas.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )
            }
        }

        if (criticos.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = alVerTodasLasAlertas)
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorRoja),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "ALERTAS ACTIVAS  ${criticos.size}",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            criticos.first().titulo,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }

        item {
            Text(
                "Tipos de desastres",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tipos.forEach { tipo ->
                    Card(
                        modifier = Modifier.clickable(onClick = alVerTodasLasAlertas),
                        colors = CardDefaults.cardColors(containerColor = SuperficieOscura),
                    ) {
                        Text(
                            etiquetaTipo(tipo),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
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
