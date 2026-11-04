package com.tripwise.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Material shape scale, used by stock M3 components. */
internal val TripWiseMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Semantic shapes for TripWise components. Read via `TripWiseTheme.shapes`. */
@Immutable
data class TripWiseShapes(
    val card: Shape = RoundedCornerShape(20.dp),
    val imageCard: Shape = RoundedCornerShape(16.dp),
    val field: Shape = RoundedCornerShape(14.dp),
    val chip: Shape = RoundedCornerShape(10.dp),
    val sheet: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    val image: Shape = RoundedCornerShape(16.dp),
    val skeleton: Shape = RoundedCornerShape(8.dp),
    val pill: Shape = CircleShape,
)

internal val LocalTripWiseShapes = staticCompositionLocalOf { TripWiseShapes() }
