package com.tripwise.feature.destination.extras

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.designsystem.component.TwFilterChip
import com.tripwise.core.designsystem.component.TwListRow
import com.tripwise.core.designsystem.component.TwTopAppBar
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.maps.GoogleMapsCard
import com.tripwise.core.maps.GoogleMapsLinks
import com.tripwise.core.maps.MapsShortcut
import com.tripwise.core.maps.openSearch
import com.tripwise.core.maps.placeLabel
import com.tripwise.core.model.Destination
import com.tripwise.core.ui.component.AttractionCard
import com.tripwise.core.ui.label.ui
import com.tripwise.core.ui.preview.PreviewData
import com.tripwise.core.ui.state.EmptyState
import com.tripwise.core.ui.state.ErrorState
import com.tripwise.core.ui.state.LoadingState
import com.tripwise.feature.destination.R

@Composable
internal fun ActivitiesScreen(destinationId: String, onBack: () -> Unit) {
    val vm = hiltViewModel<DestinationExtrasViewModel, DestinationExtrasViewModel.Factory>(key = "extras-$destinationId") { it.create(destinationId) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    ActivitiesScreen(state, vm::onAction, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ActivitiesScreen(
    state: DestinationExtrasUiState,
    onAction: (DestinationExtrasAction) -> Unit,
    onBack: () -> Unit,
) {
    val destination = (state.destination as? LoadState.Ready)?.data
    val context = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(
        topBar = {
            TwTopAppBar(
                title = destination?.let { stringResource(R.string.feature_destination_activities_title, it.name) } ?: "",
                onNavigateBack = onBack,
                navigateBackContentDescription = stringResource(R.string.feature_destination_back),
                actions = {
                    if (destination != null) {
                        IconButton(onClick = { openSearch(context, destination.mapsPlace) }) {
                            Icon(TwIcons.Map, stringResource(R.string.feature_destination_open_in_maps))
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            when (val d = state.destination) {
                LoadState.Loading -> LoadingState()
                is LoadState.Failed -> ErrorState(d.error, onRetry = { onAction(DestinationExtrasAction.Retry) })
                is LoadState.Ready -> { val loaded = d.data; if (loaded == null) {
                    EmptyState(stringResource(R.string.feature_destination_not_found_title), stringResource(R.string.feature_destination_not_found_body))
                } else {
                    PrimaryTabRow(selectedTabIndex = tab) {
                        Tab(tab == 0, { tab = 0 }, text = { Text(stringResource(R.string.feature_destination_tab_curated)) })
                        Tab(tab == 1, { tab = 1 }, text = { Text(stringResource(R.string.feature_destination_tab_google_maps)) })
                    }
                    if (tab == 0) CuratedTab(state, loaded, onAction) else GoogleMapsTab(loaded)
                } }
            }
        }
    }
}

internal val Destination.mapsPlace: String get() = placeLabel(name, region, country)

@Composable
private fun CuratedTab(state: DestinationExtrasUiState, destination: Destination, onAction: (DestinationExtrasAction) -> Unit) {
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
            // Search by name + city: curated coordinates are approximate, Google's listing is exact.
            AttractionCard(a, onShowOnMap = { openSearch(context, placeLabel(a.name, destination.name, destination.region)) })
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

/** Live places come from Google Maps itself: each row opens a Maps search around the destination. */
@Composable
private fun GoogleMapsTab(destination: Destination) {
    val context = LocalContext.current
    val place = destination.mapsPlace
    LazyColumn(contentPadding = PaddingValues(TripWiseTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm)) {
        item { GoogleMapsCard(destination.name, place, shortcuts = emptyList()) }
        item {
            Text(
                stringResource(R.string.feature_destination_google_maps_body, destination.name),
                style = TripWiseTheme.typography.bodyMedium,
                color = TripWiseTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(vertical = TripWiseTheme.spacing.sm),
            )
        }
        items(MapsShortcut.entries, key = { it.name }) { s ->
            val label = stringResource(s.label)
            TwListRow(
                title = label,
                subtitle = stringResource(R.string.feature_destination_near, destination.name),
                icon = s.icon,
                onClick = { openSearch(context, GoogleMapsLinks.nearbyQuery(s.query, place)) },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ActivitiesPreview() = TripWiseTheme {
    ActivitiesScreen(DestinationExtrasUiState(LoadState.Ready(PreviewData.destinations[1])), {}, {})
}
