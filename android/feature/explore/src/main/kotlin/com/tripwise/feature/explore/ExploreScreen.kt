package com.tripwise.feature.explore

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.designsystem.component.TwFilterChip
import com.tripwise.core.designsystem.component.TwSearchField
import com.tripwise.core.designsystem.component.TwSkeletonCard
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.model.SaveableDestination
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.ui.component.DestinationGridCard
import com.tripwise.core.ui.label.ui
import com.tripwise.core.ui.layout.DetailPane
import com.tripwise.core.ui.layout.ListDetailLayout
import com.tripwise.core.ui.preview.PreviewData
import com.tripwise.core.ui.state.EmptyState
import com.tripwise.core.ui.state.ErrorState

@Composable
internal fun ExploreScreen(
    focusSearch: Boolean,
    onDestinationClick: (String) -> Unit,
    detailPane: DetailPane?,
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ExploreScreen(state, viewModel::onAction, onDestinationClick, detailPane, focusSearch)
}

@Composable
internal fun ExploreScreen(
    state: ExploreUiState,
    onAction: (ExploreAction) -> Unit,
    onDestinationClick: (String) -> Unit,
    detailPane: DetailPane?,
    focusSearch: Boolean = false,
    modifier: Modifier = Modifier,
) {
    ListDetailLayout(
        detailPane = detailPane,
        onOpenDetail = onDestinationClick,
        emptyDetail = {
            EmptyState(
                stringResource(R.string.feature_explore_select_title),
                stringResource(R.string.feature_explore_select_body),
                icon = TwIcons.Explore,
            )
        },
        modifier = modifier,
    ) { selectedId, onItemClick, listModifier ->
        ExploreList(state, onAction, focusSearch, selectedId, onItemClick, listModifier)
    }
}

@Composable
private fun ExploreList(
    state: ExploreUiState,
    onAction: (ExploreAction) -> Unit,
    focusSearch: Boolean,
    selectedId: String?,
    onDestinationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gutter = TripWiseTheme.spacing.lg
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusSearch) { if (focusSearch) runCatching { focusRequester.requestFocus() } }

    Column(modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            stringResource(R.string.feature_explore_title),
            style = TripWiseTheme.typography.headlineMedium,
            color = TripWiseTheme.colors.primary,
            modifier = Modifier.padding(start = gutter, end = gutter, top = TripWiseTheme.spacing.lg).semantics { heading() },
        )
        TwSearchField(
            query = state.query,
            onQueryChange = { onAction(ExploreAction.QueryChanged(it)) },
            placeholder = stringResource(R.string.feature_explore_search_placeholder),
            clearContentDescription = stringResource(R.string.feature_explore_clear_search),
            focusRequester = focusRequester,
            modifier = Modifier.padding(horizontal = gutter, vertical = TripWiseTheme.spacing.md),
        )
        InterestChips(state.interest, onSelect = { onAction(ExploreAction.InterestSelected(it)) })

        when (val results = state.results) {
            LoadState.Loading -> SkeletonGrid()
            is LoadState.Failed -> ErrorState(results.error, onRetry = { onAction(ExploreAction.Retry) })
            is LoadState.Ready -> if (results.data.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.feature_explore_no_results_title),
                    body = if (state.query.isNotBlank()) {
                        stringResource(R.string.feature_explore_no_results_body, state.query.trim())
                    } else {
                        stringResource(R.string.feature_explore_no_results_filter_body)
                    },
                    icon = TwIcons.Search,
                    actionLabel = stringResource(R.string.feature_explore_clear_filters),
                    onAction = { onAction(ExploreAction.ClearFilters) },
                )
            } else {
                Text(
                    pluralStringResource(R.plurals.feature_explore_results, results.data.size, results.data.size),
                    style = TripWiseTheme.typography.labelLarge,
                    color = TripWiseTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = gutter, vertical = TripWiseTheme.spacing.sm)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
                ResultsGrid(results.data, selectedId, onDestinationClick) {
                    onAction(ExploreAction.ToggleSave(it.destination.id, !it.isSaved))
                }
            }
        }
    }
}

@Composable
private fun InterestChips(selected: TravelInterest?, onSelect: (TravelInterest?) -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = TripWiseTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
    ) {
        TwFilterChip(stringResource(R.string.feature_explore_all), selected == null, { onSelect(null) })
        TravelInterest.entries.forEach { interest ->
            TwFilterChip(
                label = stringResource(interest.ui.label),
                selected = interest == selected,
                onSelectedChange = { onSelect(if (interest == selected) null else interest) },
                leadingIcon = interest.ui.icon,
            )
        }
    }
}

@Composable
private fun ResultsGrid(
    items: List<SaveableDestination>,
    selectedId: String?,
    onClick: (String) -> Unit,
    onToggle: (SaveableDestination) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(TripWiseTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
    ) {
        items(items, key = { it.destination.id }) { item ->
            DestinationGridCard(
                item,
                onClick = { onClick(item.destination.id) },
                onToggleSave = { onToggle(item) },
                selected = item.destination.id == selectedId,
            )
        }
    }
}

@Composable
private fun SkeletonGrid() {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(TripWiseTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.xl),
        userScrollEnabled = false,
    ) {
        items(6) { TwSkeletonCard() }
    }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun ExplorePreview() = TripWiseTheme {
    Surface {
        ExploreScreen(
            ExploreUiState(results = LoadState.Ready(PreviewData.destinations.indices.map { PreviewData.saveable(it, it == 2) })),
            onAction = {}, onDestinationClick = {}, detailPane = { _, _ -> Text("Detail") },
        )
    }
}

@PreviewLightDark
@Composable
private fun ExploreEmptyPreview() = TripWiseTheme {
    Surface {
        ExploreScreen(ExploreUiState(query = "Atlantis", results = LoadState.Ready(emptyList())), {}, {}, null)
    }
}
