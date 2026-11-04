package com.tripwise.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Brand tokens Material's ColorScheme has no slot for. Read via `TripWiseTheme.extendedColors`. */
@Immutable
data class ExtendedColors(
    /** Gradient laid over hero images so white text on top stays readable (≥4.5:1 at the text). */
    val imageScrim: Brush,
    /** Text/icons drawn on top of [imageScrim]. */
    val onImage: Color,
    /** Backing for round buttons drawn on photos. */
    val overlayButton: Color,
    val rating: Color,
    /** Placeholder shown while an image loads or when it fails. */
    val imagePlaceholder: Brush,
)

internal val LightExtendedColors = ExtendedColors(
    imageScrim = Brush.verticalGradient(0f to Color.Transparent, 0.45f to Color(0x330F1D2B), 1f to Color(0xD90F1D2B)),
    onImage = Color.White,
    overlayButton = Color(0xE6F0FFFF),
    rating = RatingStar,
    imagePlaceholder = Brush.linearGradient(listOf(BrandColors.SeaBlue, BrandColors.DuskBlue)),
)

internal val DarkExtendedColors = LightExtendedColors.copy(
    overlayButton = Color(0xE60D1B2A),
    imagePlaceholder = Brush.linearGradient(listOf(Color(0xFF1E4475), Color(0xFF0D4F5B))),
)

internal val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
