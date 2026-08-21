package com.gustavomix.desastres.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class Telefono(val nombre: String, val numero: String)

private val TELEFONOS = listOf(
    Telefono("Policía", "110"),
    Telefono("Bomberos", "119"),
    Telefono("Emergencias médicas", "118"),
)

private data class Enlace(val nombre: String, val descripcion: String, val url: String)

private val ENLACES_OFICIALES = listOf(
    Enlace(
        "Defensa Civil Bolivia (VIDECI)",
        "Avisos y ayuda oficial en emergencias",
        "https://www.gob.bo",
    ),
    Enlace(
        "SENAMHI Bolivia",
        "Pronóstico del tiempo y avisos de lluvia",
        "https://senamhi.gob.bo",
    ),
    Enlace(
        "USGS",
        "Terremotos de todo el mundo, al instante",
        "https://earthquake.usgs.gov",
    ),
    Enlace(
        "GDACS",
        "Alertas globales de desastres de la ONU y la UE",
        "https://www.gdacs.org",
    ),
)

private data class Guia(val titulo: String, val pasos: List<String>)

private val GUIAS = listOf(
    Guia(
        "Si tiembla (terremoto)",
        listOf(
            "Mientras tiembla: agachate, cubrite la cabeza y agarrate de algo firme. No corras ni uses el ascensor.",
            "Alejate de ventanas, vidrios y de todo lo que se pueda caer encima.",
            "Si estás en la calle: quedate en un lugar abierto, lejos de postes, cables y paredes.",
            "Cuando pare: salí con calma por las escaleras y revisá si hay olor a gas o cables sueltos.",
            "Esperá réplicas: casi siempre vienen temblores más chicos después.",
        ),
    ),
    Guia(
        "Si hay un incendio",
        listOf(
            "Salí de inmediato y llamá a los Bomberos al 119. No vuelvas a entrar por nada.",
            "Si hay humo, andá agachado: el aire limpio queda cerca del piso.",
            "Tocá las puertas antes de abrirlas; si están calientes, buscá otra salida.",
            "Si se te prende la ropa: parate, tirate al piso y rodá.",
        ),
    ),
    Guia(
        "Si sube el agua (inundación)",
        listOf(
            "Subí a la parte más alta que puedas y cortá la luz desde el tablero si es seguro hacerlo.",
            "No cruces calles inundadas ni a pie ni en auto: el agua arrastra más de lo que parece.",
            "No tomes agua de la canilla hasta que avisen que es segura; hervila si no hay otra.",
            "Alejate de cables caídos y postes en el agua.",
        ),
    ),
    Guia(
        "Mochila de emergencia",
        listOf(
            "Agua para tres días y comida que no se eche a perder.",
            "Linterna, pilas, radio a pilas y batería externa cargada.",
            "Botiquín, tus remedios de siempre y copias de tus documentos.",
            "Abrigo, silbato para pedir ayuda y algo de dinero en efectivo.",
            "Poné un punto de encuentro con tu familia por si no hay señal.",
        ),
    ),
)

@Composable
fun PantallaRecursos(modifier: Modifier = Modifier) {
    val contexto = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 18.dp)) {
                Text(
                    "Qué hacer",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrimario,
                )
                Text(
                    "Teléfonos de emergencia y pasos concretos para cada situación.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        item {
            Text(
                "Llamar ahora",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextoPrimario,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TELEFONOS.forEach { telefono ->
                    Card(
                        modifier = Modifier.weight(1f).clickable {
                            contexto.startActivity(
                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:${telefono.numero}")),
                            )
                        },
                        colors = CardDefaults.cardColors(containerColor = SuperficieOscura),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                Icons.Filled.Call,
                                contentDescription = null,
                                tint = ColorVerde,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                telefono.numero,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextoPrimario,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            Text(
                                telefono.nombre,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextoSecundario,
                            )
                        }
                    }
                }
            }
        }
        item {
            Text(
                "Los números pueden cambiar según el lugar. Confirmá con Defensa Civil de tu región.",
                style = MaterialTheme.typography.labelSmall,
                color = TextoSecundario,
                modifier = Modifier.padding(bottom = 22.dp),
            )
        }

        item {
            Text(
                "Guías rápidas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextoPrimario,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        items(GUIAS) { guia ->
            TarjetaGuia(guia, modifier = Modifier.padding(bottom = 10.dp))
        }

        item {
            Text(
                "Páginas oficiales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextoPrimario,
                modifier = Modifier.padding(top = 14.dp, bottom = 8.dp),
            )
        }
        items(ENLACES_OFICIALES) { enlace ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable {
                        contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(enlace.url)))
                    },
                colors = CardDefaults.cardColors(containerColor = SuperficieOscura),
                shape = RoundedCornerShape(14.dp),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            enlace.nombre,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextoPrimario,
                        )
                        Text(
                            enlace.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario,
                        )
                    }
                    Icon(
                        Icons.Filled.OpenInNew,
                        contentDescription = null,
                        tint = TextoSecundario,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        item {
            Text(
                "Esta app no reemplaza a Defensa Civil ni a los servicios de emergencia. " +
                    "Ante una emergencia real, llamá siempre.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun TarjetaGuia(guia: Guia, modifier: Modifier = Modifier) {
    var abierta by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth().clickable { abierta = !abierta },
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    guia.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoPrimario,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    if (abierta) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (abierta) "Cerrar" else "Abrir",
                    tint = TextoSecundario,
                )
            }

            AnimatedVisibility(visible = abierta) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    guia.pasos.forEachIndexed { indice, paso ->
                        Row(modifier = Modifier.padding(bottom = 8.dp)) {
                            Text(
                                "${indice + 1}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AzulAcento,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                paso,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextoSecundario,
                            )
                        }
                    }
                }
            }
        }
    }
}
