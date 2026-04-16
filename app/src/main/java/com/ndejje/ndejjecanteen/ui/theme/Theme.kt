package com.ndejje.ndejjecanteen.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CanteenGreen,
    onPrimary = Color.White,
    primaryContainer = CanteenGreenContainer,
    onPrimaryContainer = CanteenGreen,
    secondary = CanteenAmber,
    onSecondary = Color.Black,
    secondaryContainer = CanteenAmberContainer,
    onSecondaryContainer = CanteenBrown,
    tertiary = CanteenBrown,
    background = CanteenSurface,
    surface = Color.White,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    error = CanteenError
)

private val DarkColorScheme = darkColorScheme(
    primary = CanteenGreenLight,
    onPrimary = Color.Black,
    primaryContainer = CanteenGreen,
    onPrimaryContainer = CanteenGreenContainer,
    secondary = CanteenAmber,
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color(0xFFF5F5F5),
    onSurface = Color(0xFFF5F5F5)
)

@Composable
fun NdejjeCanteenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = CanteenTypography,
        content = content
    )
}
