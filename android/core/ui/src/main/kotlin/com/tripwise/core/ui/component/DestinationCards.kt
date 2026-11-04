package com.tripwise.core.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tripwise.core.designsystem.component.TwOverlayIconButton
import com.tripwise.core.designsystem.component.TwPillTone
import com.tripwise.core.designsystem.component.TwRatingPill
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.model.Destination
import com.tripwise.core.model.SaveableDestination
import com.tripwise.core.ui.R
import com.tripwise.core.ui.image.RemoteImage
import com.tripwise.core.ui.preview.PreviewData

/** Heart toggle on a photo. Label/state announce e.g. "Save Bali" / "Remove Bali from saved". */
@Composable
fun SaveToggleButton(
    destinationName: String,
    isSaved: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = if (isSaved) {
        stringResource(R.string.core_ui_unsave_destination, destinationName)
    } else {
        stringResource(R.string.core_ui_save_destination, destinationName)
    }
    Box(modifier.semantics { role = Role.Switch }) {
        AnimatedContent(
            targetState = isSaved,
            transitionSpec = { scaleIn(spring<Float>(dampingRatio = 0.5f)) togetherWith fadeOut() },
            label = "save",
        ) { saved ->
            TwOverlayIconButton(
                icon = if (saved) TwIcons.Favorite else TwIcons.FavoriteOutline,
                contentDescription = label,
                onClick = onToggle,
                tint = if (saved) TripWiseTheme.colors.error else TripWiseTheme.colors.onSurface,
            )
        }
    }
}

@Composable
private fun ratingDescription(d: Destination): String? = d.rating?.let {
    stringResource(R.string.core_ui_rating_description, "%.1f".format(it), d.ratingCount ?: 0)
}

/** Grid card (Explore, Saved): 4:3 photo, heart, name, country and rating. */
@Composable
fun DestinationGridCard(
    item: SaveableDestination,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val d = item.destination
    val selectedLabel = stringResource(R.string.core_ui_selected)
    Card(
        onClick = onClick,
        modifier = modifier.semantics { if (selected) stateDescription = selectedLabel },
        shape = TripWiseTheme.shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) TripWiseTheme.colors.primaryContainer else TripWiseTheme.colors.surfaceContainerLowest,
        ),
    ) {
        Box {
            RemoteImage(
                d.coverImage, contentDescription = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(TripWiseTheme.shapes.imageCard),
            )
            SaveToggleButton(d.name, item.isSaved, onToggleSave, Modifier.align(Alignment.TopEnd).padding(TripWiseTheme.spacing.sm))
        }
        Column(Modifier.padding(TripWiseTheme.spacing.md)) {
            Text(d.name, style = TripWiseTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                d.country,
                style = TripWiseTheme.typography.bodySmall,
                color = TripWiseTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val rating = d.rating
            val description = ratingDescription(d)
            if (rating != null && description != null) {
                TwRatingPill(rating, description, Modifier.padding(top = TripWiseTheme.spacing.sm))
            }
        }
    }
}

/** Tall photo card for horizontal carousels (Home): text sits on a scrim over the image. */
@Composable
fun DestinationCarouselCard(
    item: SaveableDestination,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 168.dp,
) {
    val d = item.destination
    val description = ratingDescription(d)
    Card(
        onClick = onClick,
        modifier = modifier.width(width).aspectRatio(4f / 5f),
        shape = TripWiseTheme.shapes.card,
    ) {
        Box(Modifier.fillMaxSize()) {
            RemoteImage(d.coverImage, contentDescription = null, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(TripWiseTheme.extendedColors.imageScrim))
            SaveToggleButton(d.name, item.isSaved, onToggleSave, Modifier.align(Alignment.TopEnd).padding(TripWiseTheme.spacing.sm))
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(TripWiseTheme.spacing.md)
                    .semantics(mergeDescendants = true) {},
            ) {
                val rating = d.rating
                if (rating != null && description != null) {
                    TwRatingPill(rating, description, tone = TwPillTone.OnImage)
                }
                Text(
                    d.name,
                    style = TripWiseTheme.typography.titleMedium,
                    color = TripWiseTheme.extendedColors.onImage,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = TripWiseTheme.spacing.xs),
                )
                Text(
                    d.country,
                    style = TripWiseTheme.typography.bodySmall,
                    color = TripWiseTheme.extendedColors.onImage.copy(alpha = 0.9f),
                    maxLines = 1,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun DestinationCardsPreview() = TripWiseTheme {
    androidx.compose.foundation.layout.Row(Modifier.padding(16.dp)) {
        DestinationGridCard(PreviewData.saveable(0), {}, {}, Modifier.width(168.dp))
        DestinationCarouselCard(PreviewData.saveable(1, saved = true), {}, {}, Modifier.padding(start = 16.dp))
    }
}

