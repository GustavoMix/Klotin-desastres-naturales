package com.gustavomix.desastres.ui

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.Evento
import com.gustavomix.desastres.data.Severidad
import com.gustavomix.desastres.data.etiquetaTipo
import com.gustavomix.desastres.data.severidadDe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.hypot
import kotlin.math.min

private const val LON_BOLIVIA = -64.6
private const val LAT_BOLIVIA = -16.6

@Composable
fun PantallaMapa(
    viewModel: EventosViewModel,
    alVerAlerta: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val estado by viewModel.estado.collectAsState()

    when (val actual = estado) {
        is EstadoEventos.Cargando -> EstadoCargando(modifier)
        is EstadoEventos.Error -> EstadoError(actual.mensaje, { viewModel.cargar() }, modifier)
        is EstadoEventos.Listo -> ContenidoMapa(actual.eventos, alVerAlerta, modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoMapa(
    eventos: List<Evento>,
    alVerAlerta: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val conCoordenadas = remember(eventos) {
        eventos.filter { it.latitud != null && it.longitud != null }
    }
    val tipos = remember(eventos) { eventos.map { it.tipo }.distinct() }
    var tipoSeleccionado by remember { mutableStateOf<String?>(null) }
    var seleccionado by remember { mutableStateOf<Evento?>(null) }

    val filtrados = remember(conCoordenadas, tipoSeleccionado) {
        if (tipoSeleccionado == null) conCoordenadas
        else conCoordenadas.filter { it.tipo == tipoSeleccionado }
    }

    var escala by remember { mutableFloatStateOf(1f) }
    var desplazamiento by remember { mutableStateOf(Offset.Zero) }
    var tamanio by remember { mutableStateOf(IntSize.Zero) }

    fun centrarEn(lon: Double, lat: Double, nuevaEscala: Float) {
        if (tamanio == IntSize.Zero) return
        val base = calcularVista(tamanio, nuevaEscala, Offset.Zero)
        val destino = Offset(
            tamanio.width / 2f - ((lon + 180.0) / 360.0).toFloat() * base.ancho,
            tamanio.height / 2f - ((90.0 - lat) / 180.0).toFloat() * base.alto,
        )
        val ajustada = calcularVista(tamanio, nuevaEscala, destino)
        escala = nuevaEscala
        desplazamiento = Offset(ajustada.despX, ajustada.despY)
    }

    Column(
        modifier = modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
    ) {
        Text(
            "Mapa del mundo",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextoPrimario,
        )
        Text(
            "Cada punto es un evento. Pellizcá para acercar y tocá un punto para verlo.",
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )

        LazyRow(modifier = Modifier.padding(bottom = 12.dp)) {
            item {
                FilterChip(
                    selected = tipoSeleccionado == null,
                    onClick = { tipoSeleccionado = null; seleccionado = null },
                    label = { Text("Todos") },
                    modifier = Modifier.padding(end = 6.dp),
                )
            }
            items(tipos) { tipo ->
                FilterChip(
                    selected = tipoSeleccionado == tipo,
                    onClick = { tipoSeleccionado = tipo; seleccionado = null },
                    label = { Text(etiquetaTipo(tipo)) },
                    modifier = Modifier.padding(end = 6.dp),
                )
            }
        }

        Lienzo(
            eventos = filtrados,
            seleccionado = seleccionado,
            escala = escala,
            desplazamiento = desplazamiento,
            tamanio = tamanio,
            alCambiarTamanio = { tamanio = it },
            alTransformar = { nuevaEscala, nuevoDesplazamiento ->
                escala = nuevaEscala
                desplazamiento = nuevoDesplazamiento
            },
            alSeleccionar = { seleccionado = it },
            modifier = Modifier.fillMaxWidth().weight(1f),
        )

        Row(modifier = Modifier.padding(top = 10.dp)) {
            AssistChip(
                onClick = { escala = 1f; desplazamiento = Offset.Zero },
                label = { Text("Todo el mundo") },
                colors = AssistChipDefaults.assistChipColors(labelColor = TextoPrimario),
                modifier = Modifier.padding(end = 8.dp),
            )
            AssistChip(
                onClick = { centrarEn(LON_BOLIVIA, LAT_BOLIVIA, 8f) },
                label = { Text("Ver Bolivia") },
                colors = AssistChipDefaults.assistChipColors(labelColor = TextoPrimario),
            )
        }

        val elegido = seleccionado
        if (elegido != null) {
            TarjetaEvento(
                evento = elegido,
                onClick = { alVerAlerta(elegido.id) },
                modifier = Modifier.padding(top = 12.dp),
            )
        } else {
            Leyenda(modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Leyenda(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            "Qué significan los colores",
            style = MaterialTheme.typography.labelLarge,
            color = TextoPrimario,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Severidad.entries.forEach { severidad ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PuntoSeveridad(severidad)
                    Text(
                        etiquetaSeveridad(severidad),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSecundario,
                        modifier = Modifier.padding(start = 5.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Lienzo(
    eventos: List<Evento>,
    seleccionado: Evento?,
    escala: Float,
    desplazamiento: Offset,
    tamanio: IntSize,
    alCambiarTamanio: (IntSize) -> Unit,
    alTransformar: (Float, Offset) -> Unit,
    alSeleccionar: (Evento?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contexto = LocalContext.current
    val caminoTierra by produceState<Path?>(initialValue = null) {
        value = withContext(Dispatchers.Default) {
            runCatching { construirCaminoTierra(contexto) }.getOrNull()
        }
    }

    // Los detectores de gestos se crean una sola vez, así que tienen que leer el zoom
    // y el desplazamiento actuales en cada toque, no los que había al crearse.
    val escalaActual by rememberUpdatedState(escala)
    val desplazamientoActual by rememberUpdatedState(desplazamiento)
    val tamanioActual by rememberUpdatedState(tamanio)

    Box(
        modifier = modifier
            .heightIn(min = 200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MarOscuro)
            .onSizeChanged(alCambiarTamanio)
            .pointerInput(eventos) {
                detectTapGestures { punto ->
                    val vista = calcularVista(tamanioActual, escalaActual, desplazamientoActual)
                    var elegido: Evento? = null
                    var menorDistancia = 24.dp.toPx()
                    eventos.forEach { evento ->
                        val lat = evento.latitud ?: return@forEach
                        val lon = evento.longitud ?: return@forEach
                        val distancia = hypot(vista.x(lon) - punto.x, vista.y(lat) - punto.y)
                        if (distancia < menorDistancia) {
                            menorDistancia = distancia
                            elegido = evento
                        }
                    }
                    alSeleccionar(elegido)
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures { centro, arrastre, acercamiento, _ ->
                    val anterior = escalaActual
                    val previo = desplazamientoActual
                    val nueva = (anterior * acercamiento).coerceIn(1f, 16f)
                    val factor = nueva / anterior
                    val propuesta = Offset(
                        centro.x - (centro.x - previo.x) * factor + arrastre.x,
                        centro.y - (centro.y - previo.y) * factor + arrastre.y,
                    )
                    val vista = calcularVista(tamanioActual, nueva, propuesta)
                    alTransformar(nueva, Offset(vista.despX, vista.despY))
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val vista = calcularVista(
                IntSize(size.width.toInt(), size.height.toInt()),
                escala,
                desplazamiento,
            )

            if (vista.ancho <= 0f) return@Canvas

            caminoTierra?.let { camino ->
                withTransform({
                    translate(vista.despX, vista.despY)
                    scale(vista.ancho, vista.alto, pivot = Offset.Zero)
                }) {
                    drawPath(camino, color = TierraMapa)
                }
            }

            val radio = 4.5.dp.toPx()
            eventos.forEach { evento ->
                val lat = evento.latitud ?: return@forEach
                val lon = evento.longitud ?: return@forEach
                val cx = vista.x(lon)
                val cy = vista.y(lat)
                if (cx < -radio || cy < -radio || cx > size.width + radio || cy > size.height + radio) {
                    return@forEach
                }
                drawCircle(
                    color = colorDeSeveridad(severidadDe(evento)),
                    radius = radio,
                    center = Offset(cx, cy),
                )
            }

            seleccionado?.let { evento ->
                val lat = evento.latitud ?: return@let
                val lon = evento.longitud ?: return@let
                drawCircle(
                    color = Color.White,
                    radius = radio * 2.2f,
                    center = Offset(vista.x(lon), vista.y(lat)),
                    style = Stroke(width = 2.5.dp.toPx()),
                )
            }
        }

        if (caminoTierra == null) {
            Text(
                "Dibujando el mapa…",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                modifier = Modifier.align(Alignment.Center),
            )
        } else if (eventos.isEmpty()) {
            Text(
                "No hay eventos de este tipo con ubicación",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

/**
 * La proyección es la simple (equirectangular): la longitud va de izquierda a derecha
 * y la latitud de arriba a abajo. El mapa siempre mantiene la proporción 2:1 para que
 * los continentes no se estiren, y se centra si sobra espacio.
 */
private class VistaMapa(
    val anchoBase: Float,
    val escala: Float,
    val despX: Float,
    val despY: Float,
) {
    val ancho: Float get() = anchoBase * escala
    val alto: Float get() = anchoBase / 2f * escala

    fun x(lon: Double): Float = ((lon + 180.0) / 360.0).toFloat() * ancho + despX
    fun y(lat: Double): Float = ((90.0 - lat) / 180.0).toFloat() * alto + despY
}

private fun calcularVista(tamanio: IntSize, escala: Float, desplazamiento: Offset): VistaMapa {
    val disponibleAncho = tamanio.width.toFloat()
    val disponibleAlto = tamanio.height.toFloat()
    val anchoBase = min(disponibleAncho, disponibleAlto * 2f)
    val ancho = anchoBase * escala
    val alto = anchoBase / 2f * escala
    return VistaMapa(
        anchoBase = anchoBase,
        escala = escala,
        despX = limitar(desplazamiento.x, disponibleAncho, ancho),
        despY = limitar(desplazamiento.y, disponibleAlto, alto),
    )
}

/** Si el mapa entra entero lo centra; si no, no deja arrastrarlo más allá de sus bordes. */
private fun limitar(valor: Float, disponible: Float, tamanioMapa: Float): Float =
    if (tamanioMapa <= disponible) (disponible - tamanioMapa) / 2f
    else valor.coerceIn(disponible - tamanioMapa, 0f)

/**
 * Lee los contornos de los continentes de `assets/mundo.txt` y arma el trazo.
 * El archivo trae un anillo por línea, con pares "longitud,latitud".
 */
private fun construirCaminoTierra(contexto: Context): Path {
    val camino = Path()
    contexto.assets.open("mundo.txt").bufferedReader().forEachLine { linea ->
        val puntos = linea.trim().split(' ')
        if (puntos.size < 3) return@forEachLine
        puntos.forEachIndexed { indice, par ->
            val coma = par.indexOf(',')
            if (coma <= 0) return@forEachIndexed
            val lon = par.substring(0, coma).toFloatOrNull() ?: return@forEachIndexed
            val lat = par.substring(coma + 1).toFloatOrNull() ?: return@forEachIndexed
            // Se guardan en 0..1 para poder escalarlos después según el zoom.
            val x = (lon + 180f) / 360f
            val y = (90f - lat) / 180f
            if (indice == 0) camino.moveTo(x, y) else camino.lineTo(x, y)
        }
        camino.close()
    }
    return camino
}
