package com.gustavomix.desastres.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.gustavomix.desastres.data.Noticia
import com.gustavomix.desastres.data.horaBolivia
import com.gustavomix.desastres.data.tiempoRelativo

/**
 * Una nota de un medio.
 *
 * Abre el artículo en el navegador en vez de mostrarlo adentro: la app no es
 * dueña de ese contenido, y meterlo en un WebView sin marco haría parecer que sí.
 */
@Composable
fun TarjetaNoticia(noticia: Noticia, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { abrirEnNavegador(contexto, noticia.url) },
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column {
            if (!noticia.imagen.isNullOrBlank()) {
                Box {
                    SubcomposeAsyncImage(
                        model = noticia.imagen,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(SuperficieAlta),
                        // La portada de un artículo se cae seguido (el medio la
                        // movió, expiró el CDN). Sin imagen la tarjeta sigue
                        // sirviendo, así que el error no muestra nada.
                        error = {},
                    )
                    if (noticia.esVideo) {
                        Icon(
                            Icons.Filled.PlayCircle,
                            contentDescription = "Video",
                            tint = Color.White,
                            modifier = Modifier.align(Alignment.Center).size(46.dp),
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (noticia.esVideo && noticia.imagen.isNullOrBlank()) {
                        Icon(
                            Icons.Filled.PlayCircle,
                            contentDescription = "Video",
                            tint = AzulAcento,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        noticia.medio.ifBlank { "Medio no identificado" },
                        style = MaterialTheme.typography.labelMedium,
                        color = AzulAcento,
                    )
                    tiempoRelativo(noticia.fecha)?.let {
                        Text(
                            " · $it",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextoSecundario,
                        )
                    }
                }

                Text(
                    noticia.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoPrimario,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )

                horaBolivia(noticia.fecha)?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

/** Miniatura horizontal, para la fila de noticias dentro del detalle. */
@Composable
fun TarjetaNoticiaCompacta(noticia: Noticia, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current

    Card(
        modifier = modifier
            .width(240.dp)
            .clickable { abrirEnNavegador(contexto, noticia.url) },
        colors = CardDefaults.cardColors(containerColor = SuperficieAlta),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column {
            if (!noticia.imagen.isNullOrBlank()) {
                Box {
                    SubcomposeAsyncImage(
                        model = noticia.imagen,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(110.dp),
                        error = {},
                    )
                    if (noticia.esVideo) {
                        Icon(
                            Icons.Filled.PlayCircle,
                            contentDescription = "Video",
                            tint = Color.White,
                            modifier = Modifier.align(Alignment.Center).size(34.dp),
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    noticia.medio.ifBlank { "Medio no identificado" },
                    style = MaterialTheme.typography.labelSmall,
                    color = AzulAcento,
                )
                Text(
                    noticia.titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoPrimario,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

fun abrirEnNavegador(contexto: Context, url: String) {
    runCatching { contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}
