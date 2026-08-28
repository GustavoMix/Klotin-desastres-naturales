package com.gustavomix.desastres.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gustavomix.desastres.data.ConfiguracionMedia
import com.gustavomix.desastres.data.Evento
import com.gustavomix.desastres.data.diasTimelapse
import com.gustavomix.desastres.data.distanciaLegible
import com.gustavomix.desastres.data.etiquetaTipo
import com.gustavomix.desastres.data.explicacionSeveridad
import com.gustavomix.desastres.data.horaBoliviaLarga
import com.gustavomix.desastres.data.imagenesDeLaFuente
import com.gustavomix.desastres.data.magnitudTexto
import com.gustavomix.desastres.data.paisLegible
import com.gustavomix.desastres.data.severidadDe
import com.gustavomix.desastres.data.tiempoRelativo
import com.gustavomix.desastres.data.tituloEvento
import com.gustavomix.desastres.data.urlBusquedaVideos
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FORMATO_DIA = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es"))

/** Cuánto dura cada fotograma del timelapse. Más rápido y no se llega a mirar. */
private const val MS_POR_FOTOGRAMA = 1100L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAlertaDetalle(
    evento: Evento?,
    configuracion: ConfiguracionMedia,
    noticiasViewModel: NoticiasViewModel,
    alVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contexto = LocalContext.current
    LaunchedEffect(Unit) { noticiasViewModel.cargarSiHaceFalta() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (evento == null) "Evento" else etiquetaTipo(evento.tipo)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FondoOscuro,
                    titleContentColor = TextoPrimario,
                    navigationIconContentColor = TextoPrimario,
                    actionIconContentColor = TextoPrimario,
                ),
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (evento != null) {
                        IconButton(onClick = {
                            val texto = buildString {
                                append(tituloEvento(evento))
                                magnitudTexto(evento)?.let { append(" · $it") }
                                horaBoliviaLarga(evento.fechaEvento)?.let {
                                    append("\n$it (hora de Bolivia)")
                                }
                                evento.url?.let { append("\n$it") }
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, texto)
                            }
                            contexto.startActivity(Intent.createChooser(intent, "Compartir evento"))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Compartir")
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (evento == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No se encontró el evento", color = TextoSecundario)
            }
            return@Scaffold
        }

        val severidad = severidadDe(evento)
        val color = colorDeSeveridad(severidad)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(color).padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    iconoTipo(evento.tipo),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        etiquetaSeveridad(severidad).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        explicacionSeveridad(severidad),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.92f),
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    tituloEvento(evento),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrimario,
                )
                tiempoRelativo(evento.fechaEvento)?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.titleMedium,
                        color = color,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }

                DesdeElSatelite(
                    evento = evento,
                    configuracion = configuracion,
                    modifier = Modifier.padding(top = 18.dp),
                )

                QueDijeronLosMedios(
                    evento = evento,
                    noticiasViewModel = noticiasViewModel,
                    modifier = Modifier.padding(top = 22.dp),
                )

                HorizontalDivider(
                    color = BordeSuave,
                    modifier = Modifier.padding(vertical = 18.dp),
                )

                horaBoliviaLarga(evento.fechaEvento)?.let {
                    FilaDato("Cuándo pasó (hora de Bolivia)", it)
                }
                magnitudTexto(evento)?.let { FilaDato("Qué tan fuerte fue", it) }
                paisLegible(evento)?.let { FilaDato("País", it) }
                distanciaLegible(evento.lugar)?.let { FilaDato("Distancia del pueblo más cercano", it) }
                evento.profundidadKm?.let {
                    FilaDato("Profundidad", "${"%.0f".format(Locale.US, it)} km bajo tierra")
                }
                if (evento.latitud != null && evento.longitud != null) {
                    FilaDato(
                        "Coordenadas",
                        "%.2f, %.2f".format(Locale.US, evento.latitud, evento.longitud),
                    )
                }
                FilaDato("Quién lo reportó", evento.fuente.uppercase())

                MapasDeLaFuente(evento = evento, modifier = Modifier.padding(top = 4.dp))

                evento.url?.let { url ->
                    Button(
                        onClick = { abrirEnNavegador(contexto, url) },
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    ) {
                        Text("Ver el reporte original")
                    }
                }

                urlBusquedaVideos(evento, configuracion)?.let { url ->
                    OutlinedButton(
                        onClick = { abrirEnNavegador(contexto, url) },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    ) {
                        Icon(Icons.Filled.OndemandVideo, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Buscar videos del evento")
                    }
                    // Es una búsqueda, y lo dice. No existe ninguna fuente pública
                    // que publique video por evento; presentarlo como si lo fuera
                    // sería mentirle a alguien que quiere saber qué pasó.
                    Text(
                        "Abre una búsqueda en YouTube. La app no aloja videos: lo que aparezca " +
                            "es lo que hayan subido medios y usuarios.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

/**
 * La foto satelital y el timelapse.
 *
 * El timelapse son los días siguientes al evento, que es cuando se ve crecer el
 * incendio o avanzar el ciclón. Nunca días futuros: el mosaico de mañana todavía
 * no existe y volvería un rectángulo negro que parece un error de la app.
 */
@Composable
private fun DesdeElSatelite(
    evento: Evento,
    configuracion: ConfiguracionMedia,
    modifier: Modifier = Modifier,
) {
    val dias = remember(evento.id, configuracion) { diasTimelapse(evento, configuracion) }
    var indice by remember(evento.id) { mutableIntStateOf(0) }
    var reproduciendo by remember(evento.id) { mutableStateOf(false) }

    LaunchedEffect(reproduciendo, dias.size) {
        while (reproduciendo && dias.size > 1) {
            delay(MS_POR_FOTOGRAMA)
            indice = (indice + 1) % dias.size
        }
    }

    Column(modifier = modifier) {
        Text(
            "Desde el satélite",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextoPrimario,
        )

        FotoSatelital(
            evento = evento,
            configuracion = configuracion,
            fecha = dias.getOrNull(indice),
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(top = 10.dp),
        )

        val dia = dias.getOrNull(indice)
        Text(
            listOfNotNull(dia?.format(FORMATO_DIA), configuracion.credito).joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario,
            modifier = Modifier.padding(top = 8.dp),
        )

        if (dias.size > 1) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                IconButton(onClick = { reproduciendo = !reproduciendo }) {
                    Icon(
                        if (reproduciendo) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (reproduciendo) "Pausar" else "Ver día a día",
                        tint = AzulAcento,
                    )
                }
                Slider(
                    value = indice.toFloat(),
                    onValueChange = {
                        reproduciendo = false
                        indice = it.toInt()
                    },
                    valueRange = 0f..(dias.size - 1).toFloat(),
                    steps = (dias.size - 2).coerceAtLeast(0),
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                "Día a día desde que empezó, para ver cómo evolucionó.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
            )
        }
    }
}

/**
 * Qué escribieron los medios sobre este evento.
 *
 * Es la parte que el organismo sísmico no puede dar: si hubo heridos, cómo se
 * vio desde la calle, qué se cayó. Si no hay notas, la sección no se dibuja: un
 * encabezado vacío hace pensar que algo falló.
 */
@Composable
private fun QueDijeronLosMedios(
    evento: Evento,
    noticiasViewModel: NoticiasViewModel,
    modifier: Modifier = Modifier,
) {
    val estado by noticiasViewModel.estado.collectAsState()
    val noticias = (estado as? EstadoNoticias.Listo)?.feed?.para(evento.idAgrupado).orEmpty()
    if (noticias.isEmpty()) return

    val videos = noticias.count { it.esVideo }

    Column(modifier = modifier) {
        Text(
            "Qué dijeron los medios",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextoPrimario,
        )
        Text(
            if (videos > 0) {
                "${noticias.size} notas de prensa, $videos con video. Se abren en el navegador."
            } else {
                "${noticias.size} notas de prensa. Se abren en el navegador."
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario,
            modifier = Modifier.padding(top = 2.dp),
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 12.dp),
        ) {
            items(noticias, key = { it.url }) { noticia ->
                TarjetaNoticiaCompacta(noticia)
            }
        }
    }
}

/** Los mapas que publica la propia fuente. Hoy solo los manda GDACS. */
@Composable
private fun MapasDeLaFuente(evento: Evento, modifier: Modifier = Modifier) {
    val imagenes = remember(evento.id) { imagenesDeLaFuente(evento) }
    if (imagenes.isEmpty()) return

    val contexto = LocalContext.current

    Column(modifier = modifier.padding(top = 8.dp)) {
        Text(
            "Mapas de ${evento.fuente.uppercase()}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextoPrimario,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 10.dp),
        ) {
            items(imagenes, key = { it.url }) { imagen ->
                AsyncImage(
                    model = imagen.url,
                    contentDescription = imagen.titulo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(120.dp)
                        .width(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SuperficieAlta),
                )
            }
        }
        FilledTonalButton(
            onClick = { imagenes.firstOrNull()?.let { abrirEnNavegador(contexto, it.url) } },
            modifier = Modifier.padding(top = 10.dp),
        ) {
            Text("Abrir el mapa en grande")
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
        Text(
            valor,
            style = MaterialTheme.typography.bodyLarge,
            color = TextoPrimario,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
