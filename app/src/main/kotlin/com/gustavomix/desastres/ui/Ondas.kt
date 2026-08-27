package com.gustavomix.desastres.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.Severidad
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Cómo se dibuja un evento en movimiento.
 *
 * Un sismo se dibuja como anillos que se abren desde el epicentro, que es
 * literalmente lo que hace la onda. Un ciclón, como brazos en espiral que giran,
 * que es literalmente su forma vista desde arriba. La animación no es adorno: en
 * un mapa lleno de puntos quietos, lo que se mueve es lo que se mira, y acá lo
 * que se mueve son los eventos fuertes.
 */

/** Fase 0→1 que se repite sin cortes. Todo lo que anima comparte esta cadencia. */
@Composable
fun recordarPulso(duracionMs: Int = 2600, etiqueta: String = "pulso"): State<Float> {
    val transicion = rememberInfiniteTransition(label = etiqueta)
    return transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duracionMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = etiqueta,
    )
}

/** ¿Este evento merece animarse? Solo lo fuerte: si late todo, no late nada. */
fun seAnima(severidad: Severidad): Boolean =
    severidad == Severidad.ROJA || severidad == Severidad.NARANJA

/**
 * Anillos que se abren desde el epicentro.
 *
 * Cada anillo va corrido en la fase, así siempre hay uno nuevo saliendo mientras
 * el anterior se desvanece: el ojo lee "algo se propaga desde acá" en vez de "un
 * círculo que parpadea".
 */
fun DrawScope.dibujarOndaSismica(
    centro: Offset,
    radioMaximo: Float,
    color: Color,
    fase: Float,
    anillos: Int = 3,
    grosor: Float = 2f,
) {
    repeat(anillos) { indice ->
        val avance = (fase + indice.toFloat() / anillos) % 1f
        val radio = radioMaximo * avance
        if (radio <= 0.5f) return@repeat
        drawCircle(
            color = color.copy(alpha = (1f - avance) * 0.75f),
            radius = radio,
            center = centro,
            style = Stroke(width = grosor),
        )
    }
}

/**
 * Brazos en espiral girando, como se ve un ciclón desde el satélite.
 *
 * Es una espiral de Arquímedes —el radio crece parejo con el ángulo— y no una
 * logarítmica: a este tamaño la logarítmica amontona todo el trazo contra el
 * centro y se lee como una mancha.
 */
fun DrawScope.dibujarEspiral(
    centro: Offset,
    radio: Float,
    color: Color,
    giro: Float,
    brazos: Int = 2,
    vueltas: Float = 1.6f,
    grosor: Float = 2f,
) {
    if (radio <= 1f) return
    val pasos = 44
    val anguloTotal = vueltas * 2f * PI.toFloat()

    repeat(brazos) { brazo ->
        val desfase = giro * 2f * PI.toFloat() + brazo * (2f * PI.toFloat() / brazos)
        val camino = Path()
        for (paso in 0..pasos) {
            val avance = paso.toFloat() / pasos
            val angulo = avance * anguloTotal + desfase
            val distancia = radio * avance
            val x = centro.x + distancia * cos(angulo)
            val y = centro.y + distancia * sin(angulo)
            if (paso == 0) camino.moveTo(x, y) else camino.lineTo(x, y)
        }
        drawPath(camino, color = color.copy(alpha = 0.85f), style = Stroke(width = grosor))
    }
}

/**
 * El sello animado que va encima de la foto satelital del detalle.
 *
 * Sin él la foto es un paisaje sin referencia: el sello marca en qué punto exacto
 * de esa imagen pasó lo que se está mirando.
 */
@Composable
fun SelloDelEvento(
    tipo: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val pulso by recordarPulso()

    Canvas(modifier = modifier.fillMaxSize()) {
        val centro = Offset(size.width / 2f, size.height / 2f)
        val radioMaximo = minOf(size.width, size.height) / 2.6f
        val grosor = 2.5.dp.toPx()

        if (tipo == "ciclon") {
            dibujarEspiral(centro, radioMaximo, color, giro = pulso, grosor = grosor)
        } else {
            dibujarOndaSismica(centro, radioMaximo, color, fase = pulso, grosor = grosor)
        }

        // La animación dice "acá está pasando algo"; el punto fijo dice dónde.
        drawCircle(color = color, radius = 4.dp.toPx(), center = centro)
        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = centro,
            style = Stroke(width = 1.5.dp.toPx()),
        )
    }
}
