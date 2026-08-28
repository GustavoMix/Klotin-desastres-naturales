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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.Noticia

/**
 * Lo que publicaron los medios sobre los eventos.
 *
 * Antes esta pantalla mostraba los mismos eventos que el resto de la app con
 * otro título, que no es una sección de noticias: es la misma lista dos veces.
 * Ahora muestra artículos de prensa de verdad.
 */
@Composable
fun PantallaNoticias(
    noticiasViewModel: NoticiasViewModel,
    modifier: Modifier = Modifier,
) {
    val estado by noticiasViewModel.estado.collectAsState()
    var soloVideos by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { noticiasViewModel.cargarSiHaceFalta() }

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            Text(
                "Noticias",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextoPrimario,
            )
            Text(
                "Lo que publicaron los medios sobre los eventos más importantes.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Box(Modifier.weight(1f)) {
            when (val actual = estado) {
                is EstadoNoticias.Ocioso, is EstadoNoticias.Cargando -> EstadoCargando()
                is EstadoNoticias.Error -> EstadoError(actual.mensaje, { noticiasViewModel.recargar() })
                is EstadoNoticias.Listo -> {
                    val todas = remember(actual.feed) { actual.feed.todas() }
                    val visibles = remember(todas, soloVideos) {
                        if (soloVideos) todas.filter { it.esVideo } else todas
                    }

                    if (todas.isEmpty()) {
                        SinNoticiasTodavia()
                    } else {
                        Column(Modifier.fillMaxSize()) {
                            Filtros(
                                soloVideos = soloVideos,
                                hayVideos = todas.any { it.esVideo },
                                alCambiar = { soloVideos = it },
                            )
                            ListaDeNoticias(visibles)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Filtros(soloVideos: Boolean, hayVideos: Boolean, alCambiar: (Boolean) -> Unit) {
    LazyRow(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        item {
            FilterChip(
                selected = !soloVideos,
                onClick = { alCambiar(false) },
                label = { Text("Todo") },
                modifier = Modifier.padding(end = 6.dp),
            )
        }
        if (hayVideos) {
            item {
                FilterChip(
                    selected = soloVideos,
                    onClick = { alCambiar(true) },
                    label = { Text("Solo videos") },
                )
            }
        }
    }
}

@Composable
private fun ListaDeNoticias(noticias: List<Noticia>) {
    if (noticias.isEmpty()) {
        EstadoVacio("No hay videos entre las noticias de esta semana.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
    ) {
        items(noticias, key = { it.url }) { noticia ->
            TarjetaNoticia(noticia = noticia, modifier = Modifier.padding(bottom = 12.dp))
        }
    }
}

/**
 * Todavía no hay noticias, y se explica por qué.
 *
 * Es el estado normal hasta que el scraper corra su primera búsqueda: decir
 * "no se pudieron cargar" ahí sería mentir sobre lo que está pasando.
 */
@Composable
private fun SinNoticiasTodavia() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Text(
            "Todavía no hay noticias",
            style = MaterialTheme.typography.titleMedium,
            color = TextoPrimario,
        )
        Text(
            "El robot que junta los datos busca en los medios una vez por semana, y solo " +
                "para los eventos más fuertes o más cercanos. Cuando encuentre notas, " +
                "aparecen acá y en cada evento.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
