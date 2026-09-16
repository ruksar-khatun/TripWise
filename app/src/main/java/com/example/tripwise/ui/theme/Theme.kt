package com.example.tripwise.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DuskBlue,
    onPrimary = Color.White,
    secondary = SeaBlue,
    onSecondary = Color.White,
    tertiary = SeaSalt,
    onTertiary = Color.White,
    background = AzureMist,
    onBackground = DuskBlue,
    surface = CardWhite,
    onSurface = DuskBlue,
    surfaceVariant = Color.White,
    onSurfaceVariant = DuskBlue
)

private val DarkColorScheme = darkColorScheme(
    primary = SeaBlue,
    onPrimary = Color.White,
    secondary = SeaSalt,
    onSecondary = Color.Black,
    tertiary = DuskBlue,
    onTertiary = Color.White,
    background = Color(0xFF0F172A), // Deep navy dark background
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B), // Slate dark surface
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFF8FAFC)
)

@Composable
fun TripWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
