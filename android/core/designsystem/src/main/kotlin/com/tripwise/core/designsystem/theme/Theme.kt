package com.tripwise.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private val LightColors = lightColorScheme(
    primary = BrandColors.DuskBlue, onPrimary = BrandColors.AzureMist,
    primaryContainer = LightPrimaryContainer, onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary, onSecondary = BrandColors.AzureMist,
    secondaryContainer = BrandColors.SeaBlue, onSecondaryContainer = InkOnSeaBlue,
    tertiary = LightTertiary, onTertiary = BrandColors.AzureMist,
    tertiaryContainer = BrandColors.SeaSalt, onTertiaryContainer = InkOnSeaSalt,
    error = ErrorLight, onError = BrandColors.AzureMist,
    errorContainer = ErrorContainerLight, onErrorContainer = OnErrorContainerLight,
    background = BrandColors.AzureMist, onBackground = Ink,
    surface = BrandColors.AzureMist, onSurface = Ink,
    surfaceVariant = LightSurfaceVariant, onSurfaceVariant = InkMuted,
    surfaceContainerLowest = androidx.compose.ui.graphics.Color.White,
    surfaceContainerLow = LightContainerLow, surfaceContainer = LightContainer,
    surfaceContainerHigh = LightContainerHigh, surfaceContainerHighest = LightContainerHighest,
    outline = LightOutline, outlineVariant = LightOutlineVariant,
    inverseSurface = LightInverseSurface, inverseOnSurface = LightInverseOnSurface,
    inversePrimary = LightInversePrimary,
)

private val DarkColors = darkColorScheme(
    primary = BrandColors.SeaBlue, onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer, onPrimaryContainer = LightPrimaryContainer,
    secondary = DarkSecondary, onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer, onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary, onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer, onTertiaryContainer = DarkOnTertiaryContainer,
    error = ErrorDark, onError = OnErrorDark, errorContainer = ErrorContainerDark, onErrorContainer = ErrorContainerLight,
    background = Navy, onBackground = DarkOnSurface,
    surface = Navy, onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant, onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkContainerLowest, surfaceContainerLow = DarkContainerLow,
    surfaceContainer = DarkContainer, surfaceContainerHigh = DarkContainerHigh,
    surfaceContainerHighest = DarkContainerHighest,
    outline = DarkOutline, outlineVariant = DarkSurfaceVariant,
    inverseSurface = DarkOnSurface, inverseOnSurface = LightInverseSurface,
    inversePrimary = BrandColors.DuskBlue,
)

/**
 * Root theme for TripWise. Brand colors are fixed (no wallpaper-based dynamic color),
 * so the identity is consistent across devices.
 */
@Composable
fun TripWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalTripWiseShapes provides TripWiseShapes(),
        LocalExtendedColors provides if (darkTheme) DarkExtendedColors else LightExtendedColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
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

    val extendedColors: ExtendedColors
        @Composable @ReadOnlyComposable
        get() = LocalExtendedColors.current

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
