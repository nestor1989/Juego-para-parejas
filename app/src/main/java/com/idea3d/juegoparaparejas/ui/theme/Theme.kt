package com.idea3d.juegoparaparejas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.idea3d.juegoparaparejas.R

val Pink = Color(0xFFE01B58)
val PinkDark = Color(0xFFB81348)
val Purple = Color(0xFF7D41D3)
val Paper = Color(0xFFFFF8F6)
val Ink = Color(0xFF2B1220)
val Correct = Color(0xFF1E8F6B)
val Wrong = Color(0xFFD64545)
val Gold = Color(0xFFD4A24C)

private val LightColors = lightColorScheme(
    primary = Pink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFDE7EE),
    onPrimaryContainer = Color(0xFF7A1238),
    secondary = Purple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1E9FB),
    onSecondaryContainer = Color(0xFF4B2380),
    background = Paper,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF7EDEF),
    onSurfaceVariant = Color(0xFF6B4F5B),
    outline = Color(0xFFE3CBD3),
    error = Wrong,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF6B93),
    onPrimary = Color(0xFF3B0018),
    primaryContainer = Color(0xFF5C1230),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = Color(0xFFC9A8FF),
    onSecondary = Color(0xFF2E0F5C),
    secondaryContainer = Color(0xFF45286F),
    onSecondaryContainer = Color(0xFFEBDDFF),
    background = Color(0xFF1A0F16),
    onBackground = Color(0xFFF7E8EE),
    surface = Color(0xFF26161F),
    onSurface = Color(0xFFF7E8EE),
    surfaceVariant = Color(0xFF3A2230),
    onSurfaceVariant = Color(0xFFE2C4CF),
    outline = Color(0xFF6E4A5A),
    error = Color(0xFFFF8A80),
)

val Display = FontFamily(Font(R.font.aladin))

private val AppTypography = Typography().let { base ->
    base.copy(
        displaySmall = TextStyle(fontFamily = Display, fontSize = 40.sp, lineHeight = 46.sp),
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

@Immutable
data class Gradients(val hero: Brush, val spicy: Brush)

val LocalGradients = staticCompositionLocalOf {
    Gradients(
        hero = Brush.linearGradient(listOf(Pink, Purple)),
        spicy = Brush.linearGradient(listOf(Color(0xFF3B1428), Color(0xFF2A0E1C))),
    )
}

@Composable
fun JuegoTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
