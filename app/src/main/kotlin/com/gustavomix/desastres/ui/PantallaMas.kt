package com.gustavomix.desastres.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

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
fun PantallaMas(modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    var mostrarAcercaDe by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Más", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

        Text(
            "Mis reportes",
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable { },
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            "Sin reportes todavía.",
            style = MaterialTheme.typography.bodySmall,
        )
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
