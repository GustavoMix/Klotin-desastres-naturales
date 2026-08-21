package com.gustavomix.desastres.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.Reporte
import com.gustavomix.desastres.data.RepositorioReportes
import com.gustavomix.desastres.data.etiquetaTipo
import com.gustavomix.desastres.data.fechaLegible
import java.time.Instant

private data class ItemMenu(val titulo: String, val accion: (android.content.Context) -> Unit)

private val ITEMS = listOf(
    ItemMenu("Compartir la app") { contexto ->
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Desastres Naturales — alertas de sismos e incendios: revisala.")
        }
        contexto.startActivity(Intent.createChooser(intent, "Compartir app"))
    },
)

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

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Más", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

        Text(
            if (reportes.isEmpty()) "Mis reportes" else "Mis reportes (${reportes.size})",
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                .clickable { mostrarReportes = !mostrarReportes },
            style = MaterialTheme.typography.bodyLarge,
        )
        if (mostrarReportes) {
            if (reportes.isEmpty()) {
                Text(
                    "Sin reportes todavía. Se guardan solo en este teléfono.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            } else {
                reportes.forEach { reporte ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(
                            "${etiquetaTipo(reporte.tipo)} — ${reporte.ubicacion}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(reporte.descripcion, style = MaterialTheme.typography.bodySmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                fechaLegible(Instant.ofEpochMilli(reporte.fechaCreacion).toString())
                                    ?: "",
                                style = MaterialTheme.typography.labelSmall,
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
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        ITEMS.forEach { item ->
            Text(
                item.titulo,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable { item.accion(contexto) },
                style = MaterialTheme.typography.bodyLarge,
            )
            HorizontalDivider()
        }

        Text(
            "Acerca de la app",
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable { mostrarAcercaDe = !mostrarAcercaDe },
            style = MaterialTheme.typography.bodyLarge,
        )
        if (mostrarAcercaDe) {
            Text(
                "Desastres Naturales v0.1.0. Los datos vienen del scraper en " +
                    "github.com/GustavoMix/cron-desastres-naturales (USGS y GDACS), que corre " +
                    "una vez por semana. No es información en tiempo real.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        HorizontalDivider()
    }
}
