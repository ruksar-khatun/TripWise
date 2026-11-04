package com.tripwise.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tripwise.core.designsystem.theme.TripWiseTheme

/** A single shimmering skeleton block. Compose several to mirror the layout that is loading. */
@Composable
fun TwSkeleton(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    shape: Shape = TripWiseTheme.shapes.skeleton,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_100, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress",
    )
    val base = TripWiseTheme.colors.surfaceContainerHighest
    val highlight = TripWiseTheme.colors.surfaceContainerLow
    val x = progress * 1_400f - 400f
    Box(
        modifier
            .height(height)
            .clip(shape)
            .background(Brush.linearGradient(listOf(base, highlight, base), Offset(x, 0f), Offset(x + 400f, 0f))),
    )
}

/** Card-shaped skeleton used by list loading states. */
@Composable
fun TwSkeletonCard(modifier: Modifier = Modifier, loadingDescription: String? = null) {
    Column(
        modifier.semantics { loadingDescription?.let { contentDescription = it } },
        verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
    ) {
        TwSkeleton(Modifier.fillMaxWidth(), height = 140.dp, shape = TripWiseTheme.shapes.card)
        TwSkeleton(Modifier.fillMaxWidth(0.7f), height = 18.dp)
        TwSkeleton(Modifier.fillMaxWidth(0.4f), height = 14.dp)
    }
}

@PreviewLightDark
@Composable
private fun TwSkeletonPreview() = TripWiseTheme { TwSkeletonCard() }
