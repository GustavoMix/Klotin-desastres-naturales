package com.gustavomix.desastres.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import com.gustavomix.desastres.avisos.Avisos
import com.gustavomix.desastres.avisos.TrabajoDeAvisos
import com.gustavomix.desastres.data.AjustesAvisos
import com.gustavomix.desastres.data.PAIS_POR_DEFECTO
import com.gustavomix.desastres.data.PreferenciasAvisos
import com.gustavomix.desastres.data.Reporte
import com.gustavomix.desastres.data.RepositorioReportes
import com.gustavomix.desastres.data.Severidad
import com.gustavomix.desastres.data.etiquetaTipo
import com.gustavomix.desastres.data.horaBolivia
import java.time.Instant

@Composable
fun PantallaMas(
    modifier: Modifier = Modifier,
    senialRecarga: Any? = null,
    idsConocidos: () -> List<String> = { emptyList() },
    alVerFotos: () -> Unit = {},
) {
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

        AjustesDeAvisos(idsConocidos = idsConocidos)

        HorizontalDivider(color = BordeSuave)

        Text(
            "Fotos del satélite",
            style = MaterialTheme.typography.bodyLarge,
            color = TextoPrimario,
            modifier = Modifier.fillMaxWidth().clickable(onClick = alVerFotos).padding(vertical = 16.dp),
        )

        HorizontalDivider(color = BordeSuave)

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
                    "Los datos vienen del USGS (terremotos), del GDACS (incendios, " +
                    "inundaciones y ciclones) y de la NASA (EONET), recolectados por un robot " +
                    "que corre una vez por semana. Por eso no es información en tiempo real.\n\n" +
                    "Las fotos son imágenes reales del satélite MODIS/Terra sobre la zona del " +
                    "evento, servidas por NASA Worldview.\n\n" +
                    "Todas las horas que ves en la app están en hora de Bolivia.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
            )
        }

        HorizontalDivider(color = BordeSuave)
    }
}

/**
 * Ajustes de las notificaciones.
 *
 * Arrancan apagadas y se prenden acá. Al prenderlas se marca como visto todo lo
 * que ya está en el feed: si no, el primer chequeo encontraría catorce días de
 * eventos "nuevos" y dispararía decenas de avisos de golpe, que es exactamente la
 * forma de que alguien los apague para siempre.
 */
@Composable
private fun AjustesDeAvisos(idsConocidos: () -> List<String>) {
    val contexto = LocalContext.current
    val preferencias = remember { PreferenciasAvisos(contexto) }
    var ajustes by remember { mutableStateOf(preferencias.leer()) }
    var permisoRechazado by remember { mutableStateOf(false) }

    fun aplicar(nuevos: AjustesAvisos) {
        ajustes = nuevos
        preferencias.guardar(nuevos)
        if (nuevos.activos) TrabajoDeAvisos.programar(contexto)
        else TrabajoDeAvisos.cancelar(contexto)
    }

    fun prender() {
        permisoRechazado = false
        preferencias.marcarTodoComoVisto(idsConocidos())
        aplicar(ajustes.copy(activos = true))
    }

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { concedido ->
        if (concedido) prender() else permisoRechazado = true
    }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Avisarme de eventos nuevos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextoPrimario,
                )
                Text(
                    "Se revisa cada 6 horas. El robot que junta los datos corre una vez por " +
                        "semana, así que mirar más seguido solo gastaría batería.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Switch(
                checked = ajustes.activos,
                onCheckedChange = { prendido ->
                    if (!prendido) {
                        aplicar(ajustes.copy(activos = false))
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !Avisos.sePuedeNotificar(contexto)
                    ) {
                        pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        prender()
                    }
                },
            )
        }

        if (permisoRechazado) {
            Text(
                "Android no dio permiso para mostrar notificaciones. Se activa desde los " +
                    "ajustes del teléfono, en Notificaciones de esta app.",
                style = MaterialTheme.typography.bodySmall,
                color = ColorAmarilla,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        if (ajustes.activos) {
            Text(
                "Avisarme desde",
                style = MaterialTheme.typography.labelLarge,
                color = TextoPrimario,
                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp),
            )
            Row {
                // De más grave a menos: elegir "Moderado" incluye también lo fuerte.
                listOf(Severidad.ROJA, Severidad.NARANJA, Severidad.AMARILLA).forEach { nivel ->
                    FilterChip(
                        selected = ajustes.severidadMinima == nivel,
                        onClick = { aplicar(ajustes.copy(severidadMinima = nivel)) },
                        label = { Text(etiquetaSeveridad(nivel)) },
                        modifier = Modifier.padding(end = 6.dp),
                    )
                }
            }
            Text(
                "Incluye todo lo que sea igual o más grave que lo elegido.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                modifier = Modifier.padding(top = 6.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Solo lo que pasa en Bolivia",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextoPrimario,
                    )
                    Text(
                        "Un terremoto al otro lado del mundo no te va a despertar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario,
                    )
                }
                Switch(
                    checked = ajustes.soloMiPais,
                    onCheckedChange = {
                        aplicar(ajustes.copy(soloMiPais = it, pais = PAIS_POR_DEFECTO))
                    },
                )
            }
        }
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
