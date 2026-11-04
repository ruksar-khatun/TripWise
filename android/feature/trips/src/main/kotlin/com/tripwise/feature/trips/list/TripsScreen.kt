package com.tripwise.feature.trips.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.designsystem.component.TwSegmentedButtons
import com.tripwise.core.designsystem.component.TwSkeleton
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.ui.component.TripCard
import com.tripwise.core.ui.preview.PreviewData
import com.tripwise.core.ui.state.EmptyState
import com.tripwise.core.ui.state.ErrorState
import com.tripwise.feature.trips.R

@Composable
internal fun TripsScreen(
    onTripClick: (String) -> Unit,
    onNewTrip: () -> Unit,
    viewModel: TripsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TripsScreen(state, viewModel::onAction, onTripClick, onNewTrip)
}

@Composable
internal fun TripsScreen(
    state: TripsUiState,
    onAction: (TripsAction) -> Unit,
    onTripClick: (String) -> Unit,
    onNewTrip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewTrip,
                icon = { Icon(TwIcons.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.feature_trips_new)) },
                containerColor = TripWiseTheme.colors.primary,
                contentColor = TripWiseTheme.colors.onPrimary,
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().statusBarsPadding()) {
            Text(
                stringResource(R.string.feature_trips_title),
                style = TripWiseTheme.typography.headlineMedium,
                color = TripWiseTheme.colors.primary,
                modifier = Modifier.padding(start = TripWiseTheme.spacing.lg, top = TripWiseTheme.spacing.lg).semantics { heading() },
            )
            TwSegmentedButtons(
                options = listOf(stringResource(R.string.feature_trips_upcoming), stringResource(R.string.feature_trips_past)),
                selectedIndex = state.tab.ordinal,
                onSelect = { onAction(TripsAction.TabSelected(TripsTab.entries[it])) },
                modifier = Modifier.padding(TripWiseTheme.spacing.lg),
            )
            when (val trips = state.trips) {
                LoadState.Loading -> Column(
                    Modifier.padding(horizontal = TripWiseTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.lg),
                ) { repeat(2) { TwSkeleton(Modifier.fillMaxWidth(), height = 260.dp, shape = TripWiseTheme.shapes.card) } }
                is LoadState.Failed -> ErrorState(trips.error, onRetry = { onAction(TripsAction.Retry) })
                is LoadState.Ready -> if (trips.data.isEmpty()) {
                    if (state.tab == TripsTab.UPCOMING) {
                        EmptyState(
                            stringResource(R.string.feature_trips_empty_title),
                            stringResource(R.string.feature_trips_empty_body),
                            icon = TwIcons.Trips,
                            actionLabel = stringResource(R.string.feature_trips_plan),
                            onAction = onNewTrip,
                        )
                    } else {
                        EmptyState(
                            stringResource(R.string.feature_trips_empty_past_title),
                            stringResource(R.string.feature_trips_empty_past_body),
                            icon = TwIcons.Calendar,
                        )
                    }
                } else {
                    // 1 column on phones, 2+ on tablets.
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 340.dp),
                        contentPadding = PaddingValues(
                            start = TripWiseTheme.spacing.lg, end = TripWiseTheme.spacing.lg, bottom = 96.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.lg),
                    ) {
                        items(trips.data, key = { it.trip.id }) { item ->
                            TripCard(item.trip, item.budget, state.today, onClick = { onTripClick(item.trip.id) })
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TripsPreview() = TripWiseTheme {
    TripsScreen(
        TripsUiState(PreviewData.today, trips = LoadState.Ready(listOf(TripListItem(PreviewData.trip, PreviewData.budget)))),
        {}, {}, {},
    )
}

@PreviewLightDark
@Composable
private fun TripsEmptyPreview() = TripWiseTheme {
    TripsScreen(TripsUiState(PreviewData.today, trips = LoadState.Ready(emptyList())), {}, {}, {})
}
