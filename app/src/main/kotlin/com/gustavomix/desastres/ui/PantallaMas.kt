package com.gustavomix.desastres.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.Reporte
import com.gustavomix.desastres.data.RepositorioReportes
import com.gustavomix.desastres.data.etiquetaTipo
import com.gustavomix.desastres.data.horaBolivia
import java.time.Instant

@Composable
fun PantallaMas(modifier: Modifier = Modifier, senialRecarga: Any? = null) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioReportes(contexto) }
    var mostrarAcercaDe by remember { mutableStateOf(false) }
    var mostrarReportes by remember { mutableStateOf(false) }
    var reportes by remember { mutableStateOf(emptyList<Reporte>()) }

    LaunchedEffect(senialRecarga) {
        reportes = repositorio.obtenerTodos()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
    ) {
        Text(
            "Más",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextoPrimario,
            modifier = Modifier.padding(bottom = 18.dp),
        )

        SeccionDesplegable(
            titulo = if (reportes.isEmpty()) "Mis reportes" else "Mis reportes (${reportes.size})",
            abierta = mostrarReportes,
            alTocar = { mostrarReportes = !mostrarReportes },
        ) {
            if (reportes.isEmpty()) {
                Text(
                    "Todavía no guardaste ningún reporte. Usá el botón + para anotar lo que viste.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                )
            } else {
                reportes.forEach { reporte ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = SuperficieAlta),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "${etiquetaTipo(reporte.tipo)} — ${reporte.ubicacion}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextoPrimario,
                            )
                            Text(
                                reporte.descripcion,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSecundario,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    horaBolivia(Instant.ofEpochMilli(reporte.fechaCreacion).toString())
                                        .orEmpty(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextoSecundario,
                                )
                                TextButton(onClick = {
                                    repositorio.eliminar(reporte.id)
                                    reportes = repositorio.obtenerTodos()
                                }) {
                                    Text("Borrar")
                                }
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = BordeSuave)

        Text(
            "Compartir la app",
            style = MaterialTheme.typography.bodyLarge,
            color = TextoPrimario,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Desastres Naturales — terremotos e incendios en el mundo, con la hora de Bolivia.",
                        )
                    }
                    contexto.startActivity(Intent.createChooser(intent, "Compartir app"))
                }
                .padding(vertical = 16.dp),
        )

        HorizontalDivider(color = BordeSuave)

        SeccionDesplegable(
            titulo = "Acerca de la app",
            abierta = mostrarAcercaDe,
            alTocar = { mostrarAcercaDe = !mostrarAcercaDe },
        ) {
            Text(
                "Desastres Naturales v0.1.0.\n\n" +
                    "Los datos vienen del USGS (terremotos) y del GDACS (incendios, " +
                    "inundaciones y ciclones), recolectados por un robot que corre una vez " +
                    "por semana. Por eso no es información en tiempo real.\n\n" +
                    "Todas las horas que ves en la app están en hora de Bolivia.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
            )
        }

        HorizontalDivider(color = BordeSuave)
    }
}

@Composable
private fun SeccionDesplegable(
    titulo: String,
    abierta: Boolean,
    alTocar: () -> Unit,
    contenido: @Composable () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = alTocar)
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                titulo,
                style = MaterialTheme.typography.bodyLarge,
                color = TextoPrimario,
                modifier = Modifier.weight(1f),
            )
            Icon(
                if (abierta) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (abierta) "Cerrar" else "Abrir",
                tint = TextoSecundario,
                modifier = Modifier.size(20.dp),
            )
        }
        if (abierta) {
            Column(modifier = Modifier.padding(bottom = 12.dp)) { contenido() }
        }
    }
}
