package com.gustavomix.desastres.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.Evento
import com.gustavomix.desastres.data.Severidad
import com.gustavomix.desastres.data.horaBolivia
import com.gustavomix.desastres.data.severidadDe
import com.gustavomix.desastres.data.tiempoRelativo
import com.gustavomix.desastres.data.tituloEvento

@Composable
fun PantallaInicio(
    viewModel: EventosViewModel,
    alVerAlerta: (String) -> Unit,
    alVerTodasLasAlertas: () -> Unit,
    alVerAlertasFuertes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val estado by viewModel.estado.collectAsState()

    when (val actual = estado) {
        is EstadoEventos.Cargando -> EstadoCargando(modifier)
        is EstadoEventos.Error -> EstadoError(actual.mensaje, { viewModel.cargar() }, modifier)
        is EstadoEventos.Listo -> ContenidoInicio(
            eventos = actual.eventos,
            generado = actual.generado,
            alVerAlerta = alVerAlerta,
            alVerTodasLasAlertas = alVerTodasLasAlertas,
            alVerAlertasFuertes = alVerAlertasFuertes,
            modifier = modifier,
        )
    }
}

@Composable
private fun ContenidoInicio(
    eventos: List<Evento>,
    generado: String,
    alVerAlerta: (String) -> Unit,
    alVerTodasLasAlertas: () -> Unit,
    alVerAlertasFuertes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fuertes = remember(eventos) {
        eventos.filter { severidadDe(it) == Severidad.ROJA || severidadDe(it) == Severidad.NARANJA }
    }
    val recientes = remember(eventos) { eventos.take(8) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        // El espacio de abajo deja que el botón "+" no tape la última tarjeta.
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 18.dp)) {
                Text(
                    "Desastres Naturales",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrimario,
                )
                Text(
                    "Terremotos e incendios en el mundo, con la hora de Bolivia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario,
                    modifier = Modifier.padding(top = 4.dp),
                )
                tiempoRelativo(generado)?.let {
                    Text(
                        "Datos actualizados ${it.lowercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextoSecundario,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }

        if (fuertes.isNotEmpty()) {
            item {
                BannerAlertas(
                    cantidad = fuertes.size,
                    masReciente = fuertes.first(),
                    onClick = alVerAlertasFuertes,
                    modifier = Modifier.padding(bottom = 20.dp),
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Lo último",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoPrimario,
                )
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
                modifier = Modifier.padding(bottom = 10.dp),
            )
        }
    }
}

@Composable
private fun BannerAlertas(
    cantidad: Int,
    masReciente: Evento,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = ColorRoja, contentColor = Color.White),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (cantidad == 1) "1 evento fuerte" else "$cantidad eventos fuertes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Text(
                "El más reciente: ${tituloEvento(masReciente)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(top = 10.dp),
            )

            val cuando = listOfNotNull(
                tiempoRelativo(masReciente.fechaEvento),
                horaBolivia(masReciente.fechaEvento),
            ).joinToString(" · ")
            if (cuando.isNotBlank()) {
                Text(
                    cuando,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Text(
                "Tocá para ver todos los eventos fuertes →",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.95f),
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}
