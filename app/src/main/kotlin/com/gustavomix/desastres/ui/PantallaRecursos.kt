package com.gustavomix.desastres.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private data class Enlace(val nombre: String, val url: String)

private val ENLACES_OFICIALES = listOf(
    Enlace("Defensa Civil Bolivia (VIDECI)", "https://www.gob.bo"),
    Enlace("SENAMHI Bolivia — pronóstico", "https://senamhi.gob.bo"),
    Enlace("USGS — sismos en tiempo real", "https://earthquake.usgs.gov"),
    Enlace("GDACS — desastres globales", "https://www.gdacs.org"),
)

@Composable
fun PantallaRecursos(modifier: Modifier = Modifier) {
    val contexto = LocalContext.current

    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            Column {
                Text("Recursos útiles", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Verifica siempre estos datos con la fuente oficial: esta app no reemplaza " +
                        "a Defensa Civil ni a los servicios de emergencia.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )
            }
        }

        item {
            Text("Enlaces importantes", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        }
        items(ENLACES_OFICIALES) { enlace ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable {
                        contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(enlace.url)))
                    },
                colors = CardDefaults.cardColors(containerColor = SuperficieOscura),
            ) {
                Text(enlace.nombre, modifier = Modifier.padding(12.dp))
            }
        }

        item {
            Column {
                Text(
                    "Guías y planes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                )
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SuperficieOscura)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Guía familiar de emergencias")
                        Text(
                            "Próximamente — todavía no hay contenido propio, esto es un espacio reservado.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

