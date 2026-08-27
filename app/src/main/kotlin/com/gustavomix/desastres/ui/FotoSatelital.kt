package com.gustavomix.desastres.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.gustavomix.desastres.data.ConfiguracionMedia
import com.gustavomix.desastres.data.Evento
import com.gustavomix.desastres.data.severidadDe
import com.gustavomix.desastres.data.tieneFoto
import com.gustavomix.desastres.data.urlSatelite
import java.time.LocalDate

/**
 * La foto satelital del área del evento.
 *
 * Puede fallar por muchas razones legítimas —el satélite no pasó por ahí ese día,
 * la zona estaba de noche, el servicio de NASA está caído— así que el estado de
 * error no es una excepción rara: es un caso común y se muestra explicado, no
 * como un rectángulo roto.
 */
@Composable
fun FotoSatelital(
    evento: Evento,
    configuracion: ConfiguracionMedia,
    modifier: Modifier = Modifier,
    fecha: LocalDate? = null,
    ancho: Int = 768,
    conSello: Boolean = true,
) {
    val url = urlSatelite(evento, configuracion, fecha = fecha, ancho = ancho, alto = ancho)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SuperficieAlta),
        contentAlignment = Alignment.Center,
    ) {
        if (url == null) {
            SinFoto("Este evento no informa dónde ocurrió, así que no hay foto.")
            return@Box
        }

        SubcomposeAsyncImage(
            model = url,
            contentDescription = "Vista satelital de la zona del evento",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = { CircularProgressIndicator(color = AzulAcento) },
            error = {
                SinFoto("El satélite no tiene imagen usable de esa zona ese día.")
            },
        )

        if (conSello) {
            SelloDelEvento(
                tipo = evento.tipo,
                color = colorDeSeveridad(severidadDe(evento)),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun SinFoto(mensaje: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            mensaje,
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario,
            textAlign = TextAlign.Center,
        )
    }
}

/** Cuadradito para listas y grillas. Sin sello: a este tamaño solo sería ruido. */
@Composable
fun MiniaturaSatelital(
    evento: Evento,
    configuracion: ConfiguracionMedia,
    modifier: Modifier = Modifier,
    lado: Int = 56,
) {
    if (!tieneFoto(evento)) {
        Box(
            modifier = modifier
                .size(lado.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SuperficieAlta),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                iconoTipo(evento.tipo),
                contentDescription = null,
                tint = TextoSecundario,
                modifier = Modifier.size((lado / 3).dp),
            )
        }
        return
    }

    SubcomposeAsyncImage(
        model = urlSatelite(evento, configuracion, ancho = 256, alto = 256),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(lado.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SuperficieAlta),
        error = {
            Icon(
                iconoTipo(evento.tipo),
                contentDescription = null,
                tint = TextoSecundario,
                modifier = Modifier.size((lado / 3).dp),
            )
        },
    )
}
