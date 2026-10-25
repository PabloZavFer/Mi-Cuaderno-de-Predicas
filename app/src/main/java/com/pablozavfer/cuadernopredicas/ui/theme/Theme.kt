package com.pablozavfer.cuadernopredicas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// Paleta: fondo crema cálido, verde oscuro como color principal y dorado discreto para detalles.
private val LightColors = lightColorScheme(
    primary = Color(0xFF1E4D3A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFE6D8),
    onPrimaryContainer = Color(0xFF0B2A1D),
    secondary = Color(0xFF4E6356),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE1EADF),
    onSecondaryContainer = Color(0xFF1A2B21),
    tertiary = Color(0xFF7D5F14),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF3E3B8),
    onTertiaryContainer = Color(0xFF2C2000),
    background = Color(0xFFFAF6EE),
    onBackground = Color(0xFF1F1B16),
    surface = Color(0xFFFAF6EE),
    onSurface = Color(0xFF1F1B16),
    surfaceVariant = Color(0xFFECE5D8),
    onSurfaceVariant = Color(0xFF4D463A),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF6F1E7),
    surfaceContainer = Color(0xFFF1EBE0),
    surfaceContainerHigh = Color(0xFFEBE5D9),
    surfaceContainerHighest = Color(0xFFE5DFD3),
    outline = Color(0xFF7E7667),
    outlineVariant = Color(0xFFD3CBBC),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9FD3B6),
    onPrimary = Color(0xFF003824),
    primaryContainer = Color(0xFF1E4D3A),
    onPrimaryContainer = Color(0xFFCFE6D8),
    secondary = Color(0xFFB6CCBC),
    onSecondary = Color(0xFF213529),
    secondaryContainer = Color(0xFF374B3F),
    onSecondaryContainer = Color(0xFFD2E8D8),
    tertiary = Color(0xFFE2C27A),
    onTertiary = Color(0xFF3F2E00),
    tertiaryContainer = Color(0xFF5E4A12),
    onTertiaryContainer = Color(0xFFF3E3B8),
    background = Color(0xFF171511),
    onBackground = Color(0xFFE9E2D6),
    surface = Color(0xFF171511),
    onSurface = Color(0xFFE9E2D6),
    surfaceVariant = Color(0xFF4D463A),
    onSurfaceVariant = Color(0xFFD0C6B5),
    surfaceContainerLowest = Color(0xFF12100C),
    surfaceContainerLow = Color(0xFF1F1C17),
    surfaceContainer = Color(0xFF23201B),
    surfaceContainerHigh = Color(0xFF2E2A24),
    surfaceContainerHighest = Color(0xFF39352E),
    outline = Color(0xFF998F80),
    outlineVariant = Color(0xFF4D463A),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
)

/** Títulos con tipografía serif del sistema (sin descargar fuentes); el texto corrido en sans. */
private val AppTypography = Typography().run {
    copy(
        headlineSmall = headlineSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold),
        headlineMedium = headlineMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold),
    )
}

@Composable
fun CuadernoTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
