package com.tripwise.feature.destination

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.designsystem.component.TwButton
import com.tripwise.core.designsystem.component.TwCard
import com.tripwise.core.designsystem.component.TwInfoPill
import com.tripwise.core.designsystem.component.TwOutlinedButton
import com.tripwise.core.designsystem.component.TwOverlayIconButton
import com.tripwise.core.designsystem.component.TwPillTone
import com.tripwise.core.designsystem.component.TwRatingPill
import com.tripwise.core.designsystem.component.TwSectionHeader
import com.tripwise.core.designsystem.component.TwSkeleton
import com.tripwise.core.designsystem.component.TwTextButton
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.model.Destination
import com.tripwise.core.ui.component.AttractionCard
import com.tripwise.core.ui.component.SaveToggleButton
import com.tripwise.core.maps.GoogleMapsCard
import com.tripwise.core.maps.openSearch
import com.tripwise.core.maps.placeLabel
import androidx.compose.ui.draw.clip
import com.tripwise.core.ui.image.RemoteImage
import com.tripwise.core.ui.preview.PreviewData
import com.tripwise.core.ui.state.EmptyState
import com.tripwise.core.ui.state.ErrorState

/** Navigation out of the detail screen; wired by :app. */
data class DestinationDetailNavigation(
    val onBack: () -> Unit,
    val onPlanTrip: (destinationId: String) -> Unit,
    val onPlanWithAi: (destinationId: String) -> Unit,
    val onOpenActivities: (destinationId: String) -> Unit = {},
    /** TripMate AI about this destination; [starter] opens straight into stay/restaurant cards. */
    val onAskTripMate: (destinationId: String, prompt: String?, starter: com.tripwise.core.model.PlaceKind?) -> Unit = { _, _, _ -> },
)

/**
 * @param isPane true when shown beside a list on tablets: the back button becomes "close".
 */
@Composable
fun DestinationDetailScreen(
    destinationId: String,
    navigation: DestinationDetailNavigation,
    isPane: Boolean = false,
) {
    val viewModel = hiltViewModel<DestinationDetailViewModel, DestinationDetailViewModel.Factory>(key = destinationId) {
        it.create(destinationId)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DestinationDetailScreen(state, viewModel::onAction, navigation, isPane)
}

@Composable
internal fun DestinationDetailScreen(
    state: DestinationDetailUiState,
    onAction: (DestinationDetailAction) -> Unit,
    navigation: DestinationDetailNavigation,
    isPane: Boolean = false,
) {
    Surface(Modifier.fillMaxSize()) {
        when (state) {
            DestinationDetailUiState.Loading -> LoadingLayout()
            DestinationDetailUiState.NotFound -> Column(Modifier.statusBarsPadding()) {
                BackButton(isPane, navigation.onBack, Modifier.padding(TripWiseTheme.spacing.sm))
                EmptyState(
                    stringResource(R.string.feature_destination_not_found_title),
                    stringResource(R.string.feature_destination_not_found_body),
                    icon = TwIcons.Place,
                )
            }
            is DestinationDetailUiState.Failed -> Column(Modifier.statusBarsPadding()) {
                BackButton(isPane, navigation.onBack, Modifier.padding(TripWiseTheme.spacing.sm))
                ErrorState(state.error, onRetry = { onAction(DestinationDetailAction.Retry) })
            }
            is DestinationDetailUiState.Ready -> ReadyLayout(state, onAction, navigation, isPane)
        }
    }
}

@Composable
private fun BackButton(isPane: Boolean, onBack: () -> Unit, modifier: Modifier = Modifier) {
    TwOverlayIconButton(
        icon = if (isPane) TwIcons.Clear else TwIcons.Back,
        contentDescription = stringResource(if (isPane) R.string.feature_destination_close else R.string.feature_destination_back),
        onClick = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReadyLayout(
    state: DestinationDetailUiState.Ready,
    onAction: (DestinationDetailAction) -> Unit,
    navigation: DestinationDetailNavigation,
    isPane: Boolean,
) {
    val d = state.destination
    val context = LocalContext.current
    val listState = rememberLazyListState()
    // Solid top bar fades in once the hero has scrolled away.
    val scrolledPastHero by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val gutter = TripWiseTheme.spacing.lg

    Box(Modifier.fillMaxSize()) {
        LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 120.dp)) {
            item(key = "hero") { Hero(d) }
            item(key = "facts") {
                FlowRow(
                    Modifier.padding(horizontal = gutter, vertical = TripWiseTheme.spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
                ) {
                    TwInfoPill(stringResource(R.string.feature_destination_best_time, d.bestTimeToVisit), icon = TwIcons.Sun)
                    TwInfoPill(stringResource(R.string.feature_destination_currency, d.currency.code), icon = TwIcons.Currency)
                    TwInfoPill(
                        stringResource(R.string.feature_destination_languages, d.languages.joinToString()),
                        icon = TwIcons.Language,
                    )
                }
            }
            item(key = "about") { About(d.summary, Modifier.padding(horizontal = gutter)) }
            item(key = "highlights") {
                Section(stringResource(R.string.feature_destination_why_visit)) {
                    d.highlights.forEach { h ->
                        Row(Modifier.padding(vertical = TripWiseTheme.spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                            Icon(TwIcons.Check, contentDescription = null, tint = TripWiseTheme.colors.secondary, modifier = Modifier.size(20.dp))
                            Text(h, style = TripWiseTheme.typography.bodyLarge, modifier = Modifier.padding(start = TripWiseTheme.spacing.md))
                        }
                    }
                }
            }
            item(key = "food") {
                Section(stringResource(R.string.feature_destination_food)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
                    ) { d.localFood.forEach { TwInfoPill(it, icon = TwIcons.Food, tone = TwPillTone.Success) } }
                }
            }
            if (d.attractions.isNotEmpty()) {
                item(key = "things") {
                    Column(Modifier.padding(horizontal = gutter, vertical = TripWiseTheme.spacing.md)) {
                        HorizontalDivider(Modifier.padding(bottom = TripWiseTheme.spacing.lg), color = TripWiseTheme.colors.outlineVariant)
                        TwSectionHeader(
                            stringResource(R.string.feature_destination_things_to_do),
                            actionLabel = stringResource(R.string.feature_destination_see_all),
                            onAction = { navigation.onOpenActivities(d.id) },
                        )
                        Column(Modifier.padding(top = TripWiseTheme.spacing.sm), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md)) {
                            d.attractions.take(3).forEach { a ->
                                AttractionCard(a, onShowOnMap = { openSearch(context, placeLabel(a.name, d.name, d.region)) })
                            }
                        }
                    }
                }
            }
            item(key = "tripmate") {
                Section(stringResource(R.string.feature_destination_tripmate_title)) {
                    Text(
                        stringResource(R.string.feature_destination_tripmate_body, d.name),
                        style = TripWiseTheme.typography.bodyMedium,
                        color = TripWiseTheme.colors.onSurfaceVariant,
                    )
                    val planPrompt = stringResource(R.string.feature_destination_tripmate_plan_prompt, d.name)
                    FlowRow(
                        Modifier.padding(top = TripWiseTheme.spacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
                    ) {
                        TripMateChip(stringResource(R.string.feature_destination_tripmate_stay), TwIcons.Lodging) {
                            navigation.onAskTripMate(d.id, null, com.tripwise.core.model.PlaceKind.STAY)
                        }
                        TripMateChip(stringResource(R.string.feature_destination_tripmate_food), TwIcons.Food) {
                            navigation.onAskTripMate(d.id, null, com.tripwise.core.model.PlaceKind.FOOD)
                        }
                        TripMateChip(stringResource(R.string.feature_destination_tripmate_plan), TwIcons.Calendar) {
                            navigation.onAskTripMate(d.id, planPrompt, null)
                        }
                        TripMateChip(stringResource(R.string.feature_destination_tripmate_ask), TwIcons.Assistant) {
                            navigation.onAskTripMate(d.id, null, null)
                        }
                    }
                }
            }
            item(key = "map") {
                Section(stringResource(R.string.feature_destination_map)) {
                    GoogleMapsCard(d.name, placeLabel(d.name, d.region, d.country))
                }
            }
        }

        // Top bar: transparent over the photo, solid once scrolled.
        Surface(
            Modifier.fillMaxWidth(),
            color = if (scrolledPastHero) TripWiseTheme.colors.surface else androidx.compose.ui.graphics.Color.Transparent,
            shadowElevation = if (scrolledPastHero) 3.dp else 0.dp,
        ) {
            Row(
                Modifier.statusBarsPadding().padding(TripWiseTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BackButton(isPane, navigation.onBack)
                Text(
                    if (scrolledPastHero) d.name else "",
                    style = TripWiseTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = TripWiseTheme.spacing.md),
                )
                ShareButton(d)
                SaveToggleButton(d.name, state.isSaved, { onAction(DestinationDetailAction.ToggleSave) },
                    Modifier.padding(start = TripWiseTheme.spacing.sm))
            }
        }

        // Sticky bottom actions.
        Surface(Modifier.align(Alignment.BottomCenter).fillMaxWidth(), shadowElevation = 8.dp) {
            Row(
                Modifier.navigationBarsPadding().padding(gutter),
                horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
            ) {
                TwOutlinedButton(
                    stringResource(R.string.feature_destination_plan_ai),
                    onClick = { navigation.onPlanWithAi(d.id) },
                    leadingIcon = TwIcons.Assistant,
                    modifier = Modifier.weight(1f),
                )
                TwButton(
                    stringResource(R.string.feature_destination_plan_trip),
                    onClick = { navigation.onPlanTrip(d.id) },
                    leadingIcon = TwIcons.Add,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun Hero(d: Destination) {
    val pager = rememberPagerState(pageCount = { d.images.size.coerceAtLeast(1) })
    val photoLabel = stringResource(R.string.feature_destination_photo, d.name)
    Box(Modifier.fillMaxWidth().aspectRatio(4f / 3.4f)) {
        HorizontalPager(pager, Modifier.fillMaxSize()) { page ->
            RemoteImage(d.images.getOrNull(page), contentDescription = photoLabel, modifier = Modifier.fillMaxSize())
        }
        Box(Modifier.fillMaxSize().background(TripWiseTheme.extendedColors.imageScrim))
        Column(Modifier.align(Alignment.BottomStart).padding(TripWiseTheme.spacing.lg)) {
            Text(
                d.name,
                style = TripWiseTheme.typography.displaySmall,
                color = TripWiseTheme.extendedColors.onImage,
                modifier = Modifier.semantics { heading() },
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    d.country,
                    style = TripWiseTheme.typography.titleMedium,
                    color = TripWiseTheme.extendedColors.onImage,
                    modifier = Modifier.padding(end = TripWiseTheme.spacing.md),
                )
                val rating = d.rating
                if (rating != null) {
                    TwRatingPill(rating, stringResource(com.tripwise.core.ui.R.string.core_ui_rating_description,
                        "%.1f".format(rating), d.ratingCount ?: 0), tone = TwPillTone.OnImage)
                }
            }
            d.coverImage?.attribution?.let {
                Text(
                    stringResource(R.string.feature_destination_photo_credit, it.author, it.license.orEmpty()).trimEnd(' ', '·'),
                    style = TripWiseTheme.typography.labelSmall,
                    color = TripWiseTheme.extendedColors.onImage.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = TripWiseTheme.spacing.xs),
                )
            }
        }
        if (d.images.size > 1) {
            TwInfoPill(
                stringResource(R.string.feature_destination_photo_page, pager.currentPage + 1, d.images.size),
                Modifier.align(Alignment.BottomEnd).padding(TripWiseTheme.spacing.lg),
                tone = TwPillTone.OnImage,
            )
        }
    }
}

@Composable
private fun ShareButton(d: Destination) {
    val context = LocalContext.current
    val title = stringResource(R.string.feature_destination_share)
    val text = stringResource(R.string.feature_destination_share_text, d.name, d.country)
    TwOverlayIconButton(TwIcons.Share, title, onClick = {
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(send, title))
    })
}

@Composable
private fun About(summary: String, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(modifier.animateContentSize()) {
        TwSectionHeader(stringResource(R.string.feature_destination_about))
        Text(
            summary,
            style = TripWiseTheme.typography.bodyLarge,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = TripWiseTheme.spacing.sm),
        )
        TwTextButton(
            stringResource(if (expanded) R.string.feature_destination_read_less else R.string.feature_destination_read_more),
            onClick = { expanded = !expanded },
            modifier = Modifier.offset(x = (-12).dp),
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(horizontal = TripWiseTheme.spacing.lg, vertical = TripWiseTheme.spacing.md)) {
        HorizontalDivider(Modifier.padding(bottom = TripWiseTheme.spacing.lg), color = TripWiseTheme.colors.outlineVariant)
        TwSectionHeader(title, Modifier.padding(bottom = TripWiseTheme.spacing.sm))
        content()
    }
}

@Composable
private fun LoadingLayout() {
    Column(Modifier.fillMaxSize()) {
        TwSkeleton(Modifier.fillMaxWidth(), height = 320.dp, shape = androidx.compose.ui.graphics.RectangleShape)
        Column(Modifier.padding(TripWiseTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md)) {
            TwSkeleton(Modifier.fillMaxWidth(0.5f), height = 28.dp)
            TwSkeleton(Modifier.fillMaxWidth(), height = 16.dp)
            TwSkeleton(Modifier.fillMaxWidth(0.9f), height = 16.dp)
            TwSkeleton(Modifier.fillMaxWidth(0.7f), height = 16.dp)
        }
    }
}

@PreviewLightDark
@Composable
private fun DestinationDetailPreview() = TripWiseTheme {
    DestinationDetailScreen(
        DestinationDetailUiState.Ready(PreviewData.destinations[0], isSaved = true),
        onAction = {}, navigation = DestinationDetailNavigation({}, {}, {}),
    )
}

@PreviewLightDark
@Composable
private fun DestinationLoadingPreview() = TripWiseTheme {
    DestinationDetailScreen(DestinationDetailUiState.Loading, {}, DestinationDetailNavigation({}, {}, {}))
}

@Composable
private fun TripMateChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    androidx.compose.material3.AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(icon, contentDescription = null, Modifier.size(androidx.compose.material3.AssistChipDefaults.IconSize), tint = TripWiseTheme.colors.secondary)
        },
    )
}
