package com.tripwise.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Teal40, onPrimary = Neutral98, primaryContainer = Teal90, onPrimaryContainer = Teal10,
    secondary = Slate40, onSecondary = Neutral98, secondaryContainer = Slate90, onSecondaryContainer = Slate10,
    tertiary = Coral40, onTertiary = Neutral98, tertiaryContainer = Coral90, onTertiaryContainer = Coral10,
    error = Red40, onError = Neutral98, errorContainer = Red90, onErrorContainer = Red10,
    background = Neutral98, onBackground = Neutral10,
    surface = Neutral98, onSurface = Neutral10,
    surfaceVariant = NeutralVariant90, onSurfaceVariant = NeutralVariant30,
    surfaceContainerLowest = Neutral98, surfaceContainerLow = Neutral96, surfaceContainer = Neutral94,
    surfaceContainerHigh = Neutral92, surfaceContainerHighest = Neutral90,
    outline = NeutralVariant50, outlineVariant = NeutralVariant80,
)

private val DarkColors = darkColorScheme(
    primary = Teal80, onPrimary = Teal20, primaryContainer = Teal30, onPrimaryContainer = Teal90,
    secondary = Slate80, onSecondary = Slate20, secondaryContainer = Slate30, onSecondaryContainer = Slate90,
    tertiary = Coral80, onTertiary = Coral20, tertiaryContainer = Coral30, onTertiaryContainer = Coral90,
    error = Red80, onError = Red20, errorContainer = Red30, onErrorContainer = Red90,
    background = Neutral6, onBackground = Neutral90,
    surface = Neutral6, onSurface = Neutral90,
    surfaceVariant = NeutralVariant30, onSurfaceVariant = NeutralVariant80,
    surfaceContainerLowest = Neutral6, surfaceContainerLow = Neutral10, surfaceContainer = Neutral12,
    surfaceContainerHigh = Neutral17, surfaceContainerHighest = Neutral22,
    outline = NeutralVariant60, outlineVariant = NeutralVariant30,
)

/**
 * Root theme for TripWise.
 *
 * @param darkTheme resolved light/dark choice (the app layer maps the user's Light/Dark/System setting to this).
 * @param dynamicColor use Material You wallpaper colors on Android 12+. Off by default to keep the brand look.
 */
@Composable
fun TripWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalTripWiseShapes provides TripWiseShapes(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TripWiseTypography,
            shapes = TripWiseMaterialShapes,
            content = content,
        )
    }
}

/**
 * Single accessor for all design tokens, so screens read `TripWiseTheme.colors.primary`,
 * `TripWiseTheme.spacing.lg`, etc. and never hard-code values.
 */
object TripWiseTheme {
    val colors: ColorScheme
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: TripWiseShapes
        @Composable @ReadOnlyComposable
        get() = LocalTripWiseShapes.current

    val spacing: Spacing
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current
}
