package com.gustavomix.desastres.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AzulAcento = Color(0xFF5AB0FF)
val FondoOscuro = Color(0xFF0E1218)
val SuperficieOscura = Color(0xFF19202A)
val SuperficieAlta = Color(0xFF222B38)
val BordeSuave = Color(0xFF2C3644)

val TextoPrimario = Color(0xFFF3F6FA)
val TextoSecundario = Color(0xFFA9B5C4)

val ColorRoja = Color(0xFFF2564D)
val ColorNaranja = Color(0xFFF59A3C)
val ColorAmarilla = Color(0xFFE8C64A)
val ColorVerde = Color(0xFF4FBF87)

val MarOscuro = Color(0xFF121A24)
val TierraMapa = Color(0xFF2B3646)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulAcento,
    onPrimary = Color(0xFF06121D),
    secondary = AzulAcento,
    onSecondary = Color(0xFF06121D),
    background = FondoOscuro,
    onBackground = TextoPrimario,
    surface = SuperficieOscura,
    onSurface = TextoPrimario,
    surfaceVariant = SuperficieAlta,
    onSurfaceVariant = TextoSecundario,
    outline = BordeSuave,
    outlineVariant = BordeSuave,
    error = ColorRoja,
    onError = Color.White,
)

@Composable
fun TemaDesastres(contenido: @Composable () -> Unit) {
    MaterialTheme(colorScheme = EsquemaOscuro, content = contenido)
}
