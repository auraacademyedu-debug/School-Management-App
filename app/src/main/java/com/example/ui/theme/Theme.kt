package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    primaryContainer = RoyalBlueContainer,
    onPrimaryContainer = RoyalBlueDark,
    secondary = AmberGold,
    onSecondary = Color.White,
    secondaryContainer = AmberGoldContainer,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = Color(0xFF064E3B),
    background = CanvasSlate,
    onBackground = InkDark,
    surface = SurfaceWhite,
    onSurface = InkDark,
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = InkBody,
    outline = BorderSlate,
    error = RoseDanger,
    onError = Color.White,
    errorContainer = RoseContainer,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF064E3B),
    tertiaryContainer = Color(0xFF065F46),
    onTertiaryContainer = Color(0xFFD1FAE5),
    background = DarkCanvas,
    onBackground = Color(0xFFF8FAFC),
    surface = DarkCard,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF162033),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = DarkBorder,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

val EduPulseShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun EduPulseTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = EduPulseShapes,
        content = content
    )
}
