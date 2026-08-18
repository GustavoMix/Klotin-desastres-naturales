package com.gustavomix.desastres.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.Evento
import com.gustavomix.desastres.data.Severidad
import com.gustavomix.desastres.data.fechaLegible
import com.gustavomix.desastres.data.lugarLegible
import com.gustavomix.desastres.data.resumenClaro
import com.gustavomix.desastres.data.severidadDe

fun colorDeSeveridad(severidad: Severidad): Color = when (severidad) {
    Severidad.ROJA -> ColorRoja
    Severidad.NARANJA -> ColorNaranja
    Severidad.AMARILLA -> ColorAmarilla
    Severidad.VERDE -> ColorVerde
}

fun etiquetaSeveridad(severidad: Severidad): String = when (severidad) {
    Severidad.ROJA -> "Alerta roja"
    Severidad.NARANJA -> "Alerta naranja"
    Severidad.AMARILLA -> "Alerta amarilla"
    Severidad.VERDE -> "Alerta verde"
}

@Composable
fun InsigniaSeveridad(severidad: Severidad, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colorDeSeveridad(severidad))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(etiquetaSeveridad(severidad), style = MaterialTheme.typography.labelSmall, color = Color.White)
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

@Composable
fun TarjetaEvento(evento: Evento, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val severidad = severidadDe(evento)
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura),
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(4.dp, 48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colorDeSeveridad(severidad)),
            )
            Column(modifier = Modifier.padding(start = 12.dp).fillMaxWidth()) {
                Text(resumenClaro(evento), style = MaterialTheme.typography.titleMedium)
                lugarLegible(evento.lugar)?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
                }
                fechaLegible(evento.fechaEvento)?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
                }
                InsigniaSeveridad(severidad, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}
