package com.tripwise.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme

/** Branded gradient shown behind every remote image: while loading, offline, or if the image fails. */
@Composable
fun TwImagePlaceholder(modifier: Modifier = Modifier, showIcon: Boolean = true) {
    Box(modifier.background(TripWiseTheme.extendedColors.imagePlaceholder), contentAlignment = Alignment.Center) {
        if (showIcon) {
            Icon(
                TwIcons.Place,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = TripWiseTheme.extendedColors.onImage.copy(alpha = 0.6f),
            )
        }
    }
}
