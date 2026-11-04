package com.tripwise.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/*
 * TripWise brand colors and the tones derived from them.
 *
 * Contrast (WCAG 2.x) measured against Azure Mist:
 *   Dusk Blue 7.6:1 (text-safe)  ·  Sea Blue 2.5:1 / Sea Salt 2.4:1 (fills only; use Ink text on them, ≈6.5:1)
 * Hence: Dusk Blue = light primary; Sea Blue / Sea Salt = container fills; Sea Blue = dark primary.
 */
object BrandColors {
    val AzureMist = Color(0xFFF0FFFF)
    val DuskBlue = Color(0xFF26538D)
    val SeaBlue = Color(0xFF4BAFC0)
    val SeaSalt = Color(0xFF8BAF9D)
}

// Ink (text) tones
internal val Ink = Color(0xFF0F1D2B)
internal val InkMuted = Color(0xFF42505C)
internal val InkOnSeaBlue = Color(0xFF00222A)
internal val InkOnSeaSalt = Color(0xFF0C2017)

// Light theme
internal val LightPrimaryContainer = Color(0xFFD6E3FF)
internal val LightOnPrimaryContainer = Color(0xFF0A2A55)
internal val LightSecondary = Color(0xFF00687A) // text-safe Sea Blue (6.3:1 on Azure Mist)
internal val LightTertiary = Color(0xFF3C6352)  // text-safe Sea Salt
internal val LightSurfaceVariant = Color(0xFFDCE6EC)
internal val LightContainerLow = Color(0xFFF7FEFE)
internal val LightContainer = Color(0xFFE8F4F6)
internal val LightContainerHigh = Color(0xFFE1EEF1)
internal val LightContainerHighest = Color(0xFFDAE8EC)
internal val LightOutline = Color(0xFF72808B)   // 3.9:1, meets the 3:1 UI-component minimum
internal val LightOutlineVariant = Color(0xFFC3CFD6)
internal val LightInverseSurface = Color(0xFF24323D)
internal val LightInverseOnSurface = Color(0xFFEAF6F8)
internal val LightInversePrimary = Color(0xFFA9C7FF)

// Dark theme (derived from Dusk Blue)
internal val Navy = Color(0xFF0D1B2A)
internal val DarkOnSurface = Color(0xFFE3EEF5)
internal val DarkOnSurfaceVariant = Color(0xFFBFCAD3)
internal val DarkOnPrimary = Color(0xFF00313A)
internal val DarkPrimaryContainer = Color(0xFF1E4475)
internal val DarkSecondary = Color(0xFF8FD3DF)
internal val DarkOnSecondary = Color(0xFF00363F)
internal val DarkSecondaryContainer = Color(0xFF0D4F5B)
internal val DarkOnSecondaryContainer = Color(0xFFB8EEF7)
internal val DarkTertiary = Color(0xFFA8CCB9)
internal val DarkOnTertiary = Color(0xFF133526)
internal val DarkTertiaryContainer = Color(0xFF2C4C3D)
internal val DarkOnTertiaryContainer = Color(0xFFC4E8D4)
internal val DarkSurfaceVariant = Color(0xFF3A4752)
internal val DarkContainerLowest = Color(0xFF08131F)
internal val DarkContainerLow = Color(0xFF132332)
internal val DarkContainer = Color(0xFF172838)
internal val DarkContainerHigh = Color(0xFF21323F)
internal val DarkContainerHighest = Color(0xFF2C3D4B)
internal val DarkOutline = Color(0xFF89949E)

// Error
internal val ErrorLight = Color(0xFFBA1A1A)
internal val ErrorContainerLight = Color(0xFFFFDAD6)
internal val OnErrorContainerLight = Color(0xFF410002)
internal val ErrorDark = Color(0xFFFFB4AB)
internal val OnErrorDark = Color(0xFF690005)
internal val ErrorContainerDark = Color(0xFF93000A)

// Extras
internal val RatingStar = Color(0xFFF2B01E)
