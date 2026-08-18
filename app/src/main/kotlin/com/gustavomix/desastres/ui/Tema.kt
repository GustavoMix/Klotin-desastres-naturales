package com.gustavomix.desastres.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AzulAcento = Color(0xFF4FA8FF)
val FondoOscuro = Color(0xFF11151C)
val SuperficieOscura = Color(0xFF1B212B)

val ColorRoja = Color(0xFFE0433C)
val ColorNaranja = Color(0xFFE0862F)
val ColorAmarilla = Color(0xFFD8B93A)
val ColorVerde = Color(0xFF3FA872)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulAcento,
    background = FondoOscuro,
    surface = SuperficieOscura,
    surfaceVariant = SuperficieOscura,
    error = ColorRoja,
)

@Composable
fun TemaDesastres(contenido: @Composable () -> Unit) {
    MaterialTheme(colorScheme = EsquemaOscuro, content = contenido)
}
