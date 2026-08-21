package com.gustavomix.desastres.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.Evento
import com.gustavomix.desastres.data.distanciaLegible
import com.gustavomix.desastres.data.etiquetaTipo
import com.gustavomix.desastres.data.explicacionSeveridad
import com.gustavomix.desastres.data.horaBoliviaLarga
import com.gustavomix.desastres.data.magnitudTexto
import com.gustavomix.desastres.data.paisLegible
import com.gustavomix.desastres.data.severidadDe
import com.gustavomix.desastres.data.tiempoRelativo
import com.gustavomix.desastres.data.tituloEvento
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAlertaDetalle(evento: Evento?, alVolver: () -> Unit, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current

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

                evento.url?.let { url ->
                    Button(
                        onClick = {
                            contexto.startActivity(
                                Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)),
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    ) {
                        Text("Ver el reporte original")
                    }
                }
            }
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
