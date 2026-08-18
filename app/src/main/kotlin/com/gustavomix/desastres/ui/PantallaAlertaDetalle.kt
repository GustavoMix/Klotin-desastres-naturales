package com.gustavomix.desastres.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.Evento
import com.gustavomix.desastres.data.etiquetaTipo
import com.gustavomix.desastres.data.severidadDe

@Composable
fun PantallaAlertaDetalle(evento: Evento?, alVolver: () -> Unit, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (evento == null) "Alerta" else etiquetaTipo(evento.tipo)) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (evento != null) {
                        IconButton(onClick = {
                            val texto = buildString {
                                append(evento.titulo)
                                evento.url?.let { append("\n$it") }
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, texto)
                            }
                            contexto.startActivity(Intent.createChooser(intent, "Compartir alerta"))
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
                Text("No se encontró el evento")
            }
            return@Scaffold
        }

        val severidad = severidadDe(evento)
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(colorDeSeveridad(severidad)),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    etiquetaSeveridad(severidad).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(start = 20.dp),
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(evento.titulo, style = MaterialTheme.typography.headlineSmall)
                evento.lugar?.let {
                    Text(it, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                FilaDato("Tipo", etiquetaTipo(evento.tipo))
                FilaDato("Fuente", evento.fuente.uppercase())
                evento.fechaEvento?.let { FilaDato("Fecha", it) }
                evento.magnitud?.let {
                    FilaDato("Magnitud", "$it ${evento.unidadMagnitud.orEmpty()}")
                }

                Text(
                    "Esta información viene directo de la fuente original (${evento.fuente}); " +
                        "este evento no tiene una descripción propia todavía.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp),
                )

                evento.url?.let { url ->
                    Button(onClick = {
                        contexto.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                    }, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Ver fuente original")
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium)
        Text(valor, style = MaterialTheme.typography.bodyMedium)
    }
}
