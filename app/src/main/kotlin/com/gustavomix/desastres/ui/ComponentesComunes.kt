package com.gustavomix.desastres.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.Evento
import com.gustavomix.desastres.data.Severidad
import com.gustavomix.desastres.data.horaBolivia
import com.gustavomix.desastres.data.magnitudCorta
import com.gustavomix.desastres.data.paisSiAporta
import com.gustavomix.desastres.data.severidadDe
import com.gustavomix.desastres.data.tiempoRelativo
import com.gustavomix.desastres.data.tituloEvento

fun colorDeSeveridad(severidad: Severidad): Color = when (severidad) {
    Severidad.ROJA -> ColorRoja
    Severidad.NARANJA -> ColorNaranja
    Severidad.AMARILLA -> ColorAmarilla
    Severidad.VERDE -> ColorVerde
}

fun etiquetaSeveridad(severidad: Severidad): String = when (severidad) {
    Severidad.ROJA -> "Muy fuerte"
    Severidad.NARANJA -> "Fuerte"
    Severidad.AMARILLA -> "Moderado"
    Severidad.VERDE -> "Leve"
}

fun iconoTipo(tipo: String): ImageVector = when (tipo) {
    "sismo" -> Icons.Filled.Vibration
    "incendio" -> Icons.Filled.LocalFireDepartment
    "inundacion" -> Icons.Filled.Opacity
    "sequia" -> Icons.Filled.WbSunny
    "ciclon" -> Icons.Filled.Air
    "volcan", "derrumbe" -> Icons.Filled.Terrain
    else -> Icons.Filled.Warning
}

@Composable
fun InsigniaSeveridad(severidad: Severidad, modifier: Modifier = Modifier) {
    val color = colorDeSeveridad(severidad)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            etiquetaSeveridad(severidad),
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

@Composable
fun PuntoSeveridad(severidad: Severidad, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(colorDeSeveridad(severidad)),
    )
}

/**
 * Tarjeta de un evento: primero cuándo pasó, después qué pasó y dónde.
 * La hora siempre es la de Bolivia.
 */
@Composable
fun TarjetaEvento(evento: Evento, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val severidad = severidadDe(evento)
    val color = colorDeSeveridad(severidad)

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(width = 5.dp, height = 52.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color),
            )
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        iconoTipo(evento.tipo),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    tiempoRelativo(evento.fechaEvento)?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextoPrimario,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    magnitudCorta(evento)?.let {
                        Text(it, style = MaterialTheme.typography.labelLarge, color = color)
                    }
                }

                Text(
                    tituloEvento(evento),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoPrimario,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )

                val pais = paisSiAporta(evento)
                val hora = horaBolivia(evento.fechaEvento)
                val detalle = listOfNotNull(pais, hora).joinToString(" · ")
                if (detalle.isNotBlank()) {
                    Text(
                        detalle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }

                InsigniaSeveridad(severidad, modifier = Modifier.padding(top = 10.dp))
            }
        }
    }
}

@Composable
fun EstadoCargando(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = AzulAcento)
        Text(
            "Buscando los últimos eventos…",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
fun EstadoError(mensaje: String, alReintentar: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Filled.CloudOff,
            contentDescription = null,
            tint = TextoSecundario,
            modifier = Modifier.size(48.dp),
        )
        Text(
            "No se pudieron cargar los eventos",
            style = MaterialTheme.typography.titleMedium,
            color = TextoPrimario,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            "Revisá tu conexión a internet e intentá de nuevo.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            mensaje,
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 12.dp),
        )
        Button(onClick = alReintentar, modifier = Modifier.padding(top = 20.dp)) {
            Text("Reintentar")
        }
    }
}

@Composable
fun EstadoVacio(mensaje: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario,
        )
    }
}
