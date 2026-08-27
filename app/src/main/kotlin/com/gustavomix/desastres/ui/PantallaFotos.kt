package com.gustavomix.desastres.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.ConfiguracionMedia
import com.gustavomix.desastres.data.Evento
import com.gustavomix.desastres.data.etiquetaTipo
import com.gustavomix.desastres.data.severidadDe
import com.gustavomix.desastres.data.tieneFoto
import com.gustavomix.desastres.data.tiempoRelativo
import com.gustavomix.desastres.data.tituloEvento

/**
 * La galería: el mundo de la última quincena visto desde el satélite.
 *
 * Solo entran los eventos que tienen posición —sin coordenadas no hay foto que
 * pedir— y los más fuertes primero, porque una grilla de doscientas fotos
 * ordenada por fecha es una pared de imágenes sin jerarquía.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFotos(
    viewModel: EventosViewModel,
    alVerAlerta: (String) -> Unit,
    alVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val estado by viewModel.estado.collectAsState()
    var tipoSeleccionado by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Fotos del satélite") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FondoOscuro,
                    titleContentColor = TextoPrimario,
                    navigationIconContentColor = TextoPrimario,
                ),
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val actual = estado) {
                is EstadoEventos.Cargando -> EstadoCargando()
                is EstadoEventos.Error -> EstadoError(actual.mensaje, { viewModel.cargar() })
                is EstadoEventos.Listo -> {
                    val conFoto = remember(actual.eventos) {
                        actual.eventos
                            .filter { tieneFoto(it) }
                            .sortedWith(
                                compareBy<Evento> { severidadDe(it).ordinal }
                                    .thenByDescending { it.fechaEvento ?: "" },
                            )
                    }
                    val tipos = remember(conFoto) { conFoto.map { it.tipo }.distinct() }
                    val visibles = remember(conFoto, tipoSeleccionado) {
                        if (tipoSeleccionado == null) conFoto
                        else conFoto.filter { it.tipo == tipoSeleccionado }
                    }

                    Column(Modifier.fillMaxSize()) {
                        Text(
                            "Cada foto es la imagen real del satélite sobre esa zona, el día que " +
                                "pasó. ${actual.media.credito}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        )

                        LazyRow(modifier = Modifier.padding(start = 16.dp, bottom = 10.dp)) {
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

                        if (visibles.isEmpty()) {
                            EstadoVacio("No hay eventos con ubicación de este tipo.")
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 150.dp),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 96.dp,
                                ),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                items(visibles, key = { it.id }) { evento ->
                                    Baldosa(
                                        evento = evento,
                                        configuracion = actual.media,
                                        onClick = { alVerAlerta(evento.id) },
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

@Composable
private fun Baldosa(
    evento: Evento,
    configuracion: ConfiguracionMedia,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
    ) {
        FotoSatelital(
            evento = evento,
            configuracion = configuracion,
            ancho = 384,
            // A este tamaño el sello animado taparía la foto en vez de ubicarla.
            conSello = false,
            modifier = Modifier.fillMaxSize(),
        )

        // Degradado abajo: sin él el texto blanco desaparece sobre una nube.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.55f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.78f),
                    ),
                ),
        )

        Column(modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)) {
            Text(
                tituloEvento(evento),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            tiempoRelativo(evento.fechaEvento)?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }

        PuntoSeveridad(
            severidadDe(evento),
            modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
        )
    }
}
