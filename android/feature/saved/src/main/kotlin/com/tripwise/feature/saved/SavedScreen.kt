package com.tripwise.feature.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.designsystem.component.TwSkeletonCard
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.model.SaveableDestination
import com.tripwise.core.model.SavedDestination
import com.tripwise.core.ui.component.DestinationGridCard
import com.tripwise.core.ui.layout.DetailPane
import com.tripwise.core.ui.layout.ListDetailLayout
import com.tripwise.core.ui.preview.PreviewData
import com.tripwise.core.ui.state.EmptyState
import com.tripwise.core.ui.state.ErrorState
import java.time.Instant

@Composable
internal fun SavedScreen(
    onDestinationClick: (String) -> Unit,
    onExploreClick: () -> Unit,
    detailPane: DetailPane?,
    viewModel: SavedViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SavedScreen(state, viewModel::onAction, onDestinationClick, onExploreClick, detailPane)
}

@Composable
internal fun SavedScreen(
    state: SavedUiState,
    onAction: (SavedAction) -> Unit,
    onDestinationClick: (String) -> Unit,
    onExploreClick: () -> Unit,
    detailPane: DetailPane?,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val removed = state.lastRemoved
    val removedMessage = removed?.let { stringResource(R.string.feature_saved_removed, it.destination.name) }
    val undoLabel = stringResource(R.string.feature_saved_undo)
    LaunchedEffect(removed) {
        if (removed != null && removedMessage != null) {
            val result = snackbarHostState.showSnackbar(removedMessage, undoLabel, duration = SnackbarDuration.Long)
            onAction(if (result == SnackbarResult.ActionPerformed) SavedAction.UndoRemove else SavedAction.UndoDismissed)
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
    ) { padding ->
        ListDetailLayout(
            detailPane = detailPane,
            onOpenDetail = onDestinationClick,
            emptyDetail = {
                EmptyState(
                    stringResource(R.string.feature_saved_select_title),
                    stringResource(R.string.feature_saved_select_body),
                    icon = TwIcons.Saved,
                )
            },
            modifier = Modifier.padding(padding),
        ) { selectedId, onItemClick, listModifier ->
            Column(listModifier.fillMaxSize().statusBarsPadding()) {
                TitleRow(state.sort, onSort = { onAction(SavedAction.SortChanged(it)) })
                when (val items = state.items) {
                    LoadState.Loading -> LazyVerticalGrid(
                        GridCells.Adaptive(160.dp),
                        contentPadding = PaddingValues(TripWiseTheme.spacing.lg),
                        horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.xl),
                        userScrollEnabled = false,
                    ) { items(4) { TwSkeletonCard() } }
                    is LoadState.Failed -> ErrorState(items.error, onRetry = { onAction(SavedAction.Retry) })
                    is LoadState.Ready -> if (items.data.isEmpty()) {
                        EmptyState(
                            title = stringResource(R.string.feature_saved_empty_title),
                            body = stringResource(R.string.feature_saved_empty_body),
                            icon = TwIcons.Saved,
                            actionLabel = stringResource(R.string.feature_saved_explore),
                            onAction = onExploreClick,
                        )
                    } else {
                        LazyVerticalGrid(
                            GridCells.Adaptive(160.dp),
                            contentPadding = PaddingValues(TripWiseTheme.spacing.lg),
                            horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
                            verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
                        ) {
                            items(items.data, key = { it.destination.id }) { saved ->
                                DestinationGridCard(
                                    SaveableDestination(saved.destination, isSaved = true),
                                    onClick = { onItemClick(saved.destination.id) },
                                    onToggleSave = { onAction(SavedAction.Remove(saved)) },
                                    selected = saved.destination.id == selectedId,
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TitleRow(sort: SavedSort, onSort: (SavedSort) -> Unit) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    Row(
        Modifier.padding(start = TripWiseTheme.spacing.lg, end = TripWiseTheme.spacing.sm, top = TripWiseTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.feature_saved_title),
            style = TripWiseTheme.typography.headlineMedium,
            color = TripWiseTheme.colors.primary,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        Box {
            TextButton(onClick = { menuOpen = true }) {
                Icon(TwIcons.Sort, contentDescription = null)
                Text(
                    stringResource(if (sort == SavedSort.RECENT) R.string.feature_saved_sort_recent else R.string.feature_saved_sort_name),
                    Modifier.padding(start = TripWiseTheme.spacing.xs),
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.feature_saved_sort_recent)) },
                    onClick = { onSort(SavedSort.RECENT); menuOpen = false },
                    trailingIcon = { if (sort == SavedSort.RECENT) Icon(TwIcons.Check, null) },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.feature_saved_sort_name)) },
                    onClick = { onSort(SavedSort.NAME); menuOpen = false },
                    trailingIcon = { if (sort == SavedSort.NAME) Icon(TwIcons.Check, null) },
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SavedPreview() = TripWiseTheme {
    Surface {
        SavedScreen(
            SavedUiState(items = LoadState.Ready(PreviewData.destinations.map { SavedDestination(it, Instant.EPOCH) })),
            {}, {}, {}, null,
        )
    }
}

@PreviewLightDark
@Composable
private fun SavedEmptyPreview() = TripWiseTheme {
    Surface { SavedScreen(SavedUiState(items = LoadState.Ready(emptyList())), {}, {}, {}, null) }
}
