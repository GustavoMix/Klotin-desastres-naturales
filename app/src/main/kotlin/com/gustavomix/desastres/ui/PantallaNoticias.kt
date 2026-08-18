package com.gustavomix.desastres.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.etiquetaTipo

@Composable
fun PantallaNoticias(
    viewModel: EventosViewModel,
    alVerAlerta: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val estado by viewModel.estado.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            "Esta sección muestra los eventos del feed como si fueran noticias — el cron " +
                "todavía no trae una fuente de noticias real.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp),
        )

        when (val actual = estado) {
            is EstadoEventos.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is EstadoEventos.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No se pudo cargar: ${actual.mensaje}")
            }
            is EstadoEventos.Listo -> {
                val tipos = remember(actual.eventos) { actual.eventos.map { it.tipo }.distinct() }
                var tipoSeleccionado by remember { mutableStateOf<String?>(null) }
                val filtrados = if (tipoSeleccionado == null) {
                    actual.eventos
                } else {
                    actual.eventos.filter { it.tipo == tipoSeleccionado }
                }

                LazyRow(modifier = Modifier.padding(horizontal = 12.dp)) {
                    item {
                        FilterChip(
                            selected = tipoSeleccionado == null,
                            onClick = { tipoSeleccionado = null },
                            label = { Text("Recientes") },
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
