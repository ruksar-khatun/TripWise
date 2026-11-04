package com.tripwise.feature.destination.extras

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.designsystem.component.TwFilterChip
import com.tripwise.core.designsystem.component.TwRatingPill
import com.tripwise.core.designsystem.component.TwTextButton
import com.tripwise.core.designsystem.component.TwTopAppBar
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.maps.openDirections
import com.tripwise.core.model.NearbyPlace
import com.tripwise.core.ui.component.AttractionCard
import com.tripwise.core.ui.label.ui
import com.tripwise.core.ui.preview.PreviewData
import com.tripwise.core.ui.state.EmptyState
import com.tripwise.core.ui.state.ErrorState
import com.tripwise.core.ui.state.LoadingState
import com.tripwise.core.ui.state.SkeletonListState
import com.tripwise.feature.destination.R

@Composable
internal fun ActivitiesScreen(destinationId: String, onBack: () -> Unit, onOpenMap: () -> Unit) {
    val vm = hiltViewModel<DestinationExtrasViewModel, DestinationExtrasViewModel.Factory>(key = "extras-$destinationId") { it.create(destinationId) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    ActivitiesScreen(state, vm::onAction, onBack, onOpenMap)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ActivitiesScreen(
    state: DestinationExtrasUiState,
    onAction: (DestinationExtrasAction) -> Unit,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
) {
    val destination = (state.destination as? LoadState.Ready)?.data
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(
        topBar = {
            TwTopAppBar(
                title = destination?.let { stringResource(R.string.feature_destination_activities_title, it.name) } ?: "",
                onNavigateBack = onBack,
                navigateBackContentDescription = stringResource(R.string.feature_destination_back),
                actions = {
                    androidx.compose.material3.IconButton(onClick = onOpenMap) {
                        androidx.compose.material3.Icon(TwIcons.Map, stringResource(com.tripwise.core.maps.R.string.core_maps_open_map))
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            when (val d = state.destination) {
                LoadState.Loading -> LoadingState()
                is LoadState.Failed -> ErrorState(d.error, onRetry = { onAction(DestinationExtrasAction.Retry) })
                is LoadState.Ready -> if (d.data == null) {
                    EmptyState(stringResource(R.string.feature_destination_not_found_title), stringResource(R.string.feature_destination_not_found_body))
                } else {
                    PrimaryTabRow(selectedTabIndex = tab) {
                        Tab(tab == 0, { tab = 0 }, text = { Text(stringResource(R.string.feature_destination_tab_curated)) })
                        Tab(tab == 1, { tab = 1 }, text = { Text(stringResource(R.string.feature_destination_tab_nearby)) })
                    }
                    if (tab == 0) CuratedTab(state, onAction) else NearbyTab(state, d.data.name, onAction)
                }
            }
        }
    }
}

@Composable
private fun CuratedTab(state: DestinationExtrasUiState, onAction: (DestinationExtrasAction) -> Unit) {
    val destination = (state.destination as LoadState.Ready).data ?: return
    val context = LocalContext.current
    val items = destination.attractions.filter { state.category == null || it.category == state.category }
    LazyColumn(contentPadding = PaddingValues(TripWiseTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md)) {
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm)) {
                TwFilterChip(stringResource(R.string.feature_destination_all_categories), state.category == null, { onAction(DestinationExtrasAction.SelectCategory(null)) })
                state.categories.forEach { c ->
                    TwFilterChip(stringResource(c.ui.label), state.category == c, { onAction(DestinationExtrasAction.SelectCategory(if (state.category == c) null else c)) }, leadingIcon = c.ui.icon)
                }
            }
        }
        items(items, key = { it.id }) { a ->
            AttractionCard(a, onShowOnMap = { openDirections(context, a.location, a.name) })
        }
        item {
            Text(
                stringResource(R.string.feature_destination_approx_note),
                style = TripWiseTheme.typography.bodySmall,
                color = TripWiseTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NearbyTab(state: DestinationExtrasUiState, name: String, onAction: (DestinationExtrasAction) -> Unit) {
    LaunchedEffect(Unit) { onAction(DestinationExtrasAction.LoadNearby) }
    when (val nearby = state.nearby) {
        null, LoadState.Loading -> SkeletonListState()
        is LoadState.Failed -> ErrorState(nearby.error, onRetry = { onAction(DestinationExtrasAction.Retry) })
        is LoadState.Ready -> if (nearby.data.isEmpty()) {
            EmptyState(stringResource(R.string.feature_destination_nearby_empty), stringResource(R.string.feature_destination_nearby_body, name), icon = TwIcons.Place)
        } else {
            LazyColumn(contentPadding = PaddingValues(TripWiseTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md)) {
                item {
                    Text(stringResource(R.string.feature_destination_nearby_body, name), style = TripWiseTheme.typography.bodyMedium, color = TripWiseTheme.colors.onSurfaceVariant)
                }
                items(nearby.data, key = { it.id }) { NearbyPlaceRow(it) }
            }
        }
    }
}

@Composable
internal fun NearbyPlaceRow(place: NearbyPlace, modifier: Modifier = Modifier) {
    val uri = LocalUriHandler.current
    val context = LocalContext.current
    Surface(modifier.fillMaxWidth(), shape = TripWiseTheme.shapes.card, color = TripWiseTheme.colors.surfaceContainerLowest) {
        Column(Modifier.padding(TripWiseTheme.spacing.lg).semantics(mergeDescendants = true) {}) {
            Text(place.name, style = TripWiseTheme.typography.titleMedium)
            listOfNotNull(place.category, place.address).joinToString(" · ").takeIf { it.isNotEmpty() }?.let {
                Text(it, style = TripWiseTheme.typography.bodySmall, color = TripWiseTheme.colors.onSurfaceVariant)
            }
            Row(Modifier.padding(top = TripWiseTheme.spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                place.rating?.let { r ->
                    TwRatingPill(r, stringResource(com.tripwise.core.ui.R.string.core_ui_rating_description, "%.1f".format(r), place.ratingCount ?: 0))
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                    TwTextButton(stringResource(R.string.feature_destination_open_in_maps), onClick = {
                        place.mapsUri?.let { runCatching { uri.openUri(it) } } ?: openDirections(context, place.location, place.name)
                    })
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ActivitiesPreview() = TripWiseTheme {
    ActivitiesScreen(DestinationExtrasUiState(LoadState.Ready(PreviewData.destinations[1])), {}, {}, {})
}
