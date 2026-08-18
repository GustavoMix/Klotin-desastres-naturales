package com.gustavomix.desastres.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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

@Composable
fun PantallaEventos(
    modifier: Modifier = Modifier,
    viewModel: EventosViewModel,
) {
    val estado by viewModel.estado.collectAsState()

    when (val actual = estado) {
        is EstadoEventos.Cargando -> Cargando(modifier)
        is EstadoEventos.Error -> ErrorConReintento(modifier, actual.mensaje) { viewModel.cargar() }
        is EstadoEventos.Listo -> ListaEventos(modifier, actual.eventos)
    }
}

@Composable
private fun Cargando(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorConReintento(modifier: Modifier = Modifier, mensaje: String, onReintentar: () -> Unit) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No se pudieron cargar los eventos")
            Text(mensaje, style = MaterialTheme.typography.bodySmall)
            Button(onClick = onReintentar, modifier = Modifier.padding(top = 12.dp)) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun ListaEventos(modifier: Modifier = Modifier, eventos: List<Evento>) {
    if (eventos.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay eventos por ahora")
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(eventos, key = { it.id }) { evento -> TarjetaEvento(evento) }
    }
}

@Composable
private fun TarjetaEvento(evento: Evento) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(evento.titulo, style = MaterialTheme.typography.titleMedium)
            evento.lugar?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            val detalle = buildString {
                append(evento.tipo)
                evento.magnitud?.let { append(" · $it ${evento.unidadMagnitud.orEmpty()}") }
                evento.fechaEvento?.let { append(" · $it") }
            }
            Text(detalle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
