package com.tripwise.feature.trips.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.designsystem.component.TwButton
import com.tripwise.core.designsystem.component.TwCard
import com.tripwise.core.designsystem.component.TwFilterChip
import com.tripwise.core.designsystem.component.TwOutlinedButton
import com.tripwise.core.designsystem.component.TwOverlayIconButton
import com.tripwise.core.designsystem.component.TwProgressBar
import com.tripwise.core.designsystem.component.TwSectionHeader
import com.tripwise.core.designsystem.component.TwSkeleton
import com.tripwise.core.designsystem.component.TwStatTile
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.model.CategoryBudget
import com.tripwise.core.model.ItineraryItem
import com.tripwise.core.ui.component.BudgetProgress
import com.tripwise.core.ui.component.TripStatusPill
import com.tripwise.core.ui.format.Formatters
import com.tripwise.core.ui.format.currentLocale
import com.tripwise.core.ui.image.RemoteImage
import com.tripwise.core.ui.label.ui
import com.tripwise.core.ui.preview.PreviewData
import com.tripwise.core.ui.state.EmptyState
import com.tripwise.core.ui.state.ErrorState
import com.tripwise.feature.trips.R
import kotlinx.coroutines.launch

@Composable
internal fun TripDetailScreen(
    onBack: () -> Unit,
    onPlanWithAi: (tripId: String) -> Unit,
    viewModel: TripDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TripDetailScreen(state, viewModel::onAction, onBack, onPlanWithAi)
}

@Composable
internal fun TripDetailScreen(
    state: TripDetailUiState,
    onAction: (TripDetailAction) -> Unit,
    onBack: () -> Unit,
    onPlanWithAi: (tripId: String) -> Unit,
) {
    var deleteRequested by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.feature_trips_delete_title)) },
            text = { Text(stringResource(R.string.feature_trips_delete_body)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    deleteRequested = true
                    onAction(TripDetailAction.Delete)
                }) { Text(stringResource(R.string.feature_trips_delete), color = TripWiseTheme.colors.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.feature_trips_cancel)) } },
        )
    }
    Surface(Modifier.fillMaxSize()) {
        when (state) {
            TripDetailUiState.Loading -> Column {
                TwSkeleton(Modifier.fillMaxWidth(), height = 220.dp, shape = androidx.compose.ui.graphics.RectangleShape)
                TwSkeleton(Modifier.padding(TripWiseTheme.spacing.lg).fillMaxWidth(), height = 48.dp)
            }
            // Also reached right after deleting: leave the screen instead of showing "not found".
            TripDetailUiState.NotFound -> if (deleteRequested) {
                LaunchedEffect(Unit) { onBack() }
            } else Column(Modifier.statusBarsPadding()) {
                BackButton(onBack)
                EmptyState(stringResource(R.string.feature_trips_not_found_title), stringResource(R.string.feature_trips_not_found_body), icon = TwIcons.Trips)
            }
            is TripDetailUiState.Failed -> Column(Modifier.statusBarsPadding()) {
                BackButton(onBack)
                ErrorState(state.error, onRetry = { onAction(TripDetailAction.Retry) })
            }
            is TripDetailUiState.Ready -> Ready(state, onAction, onBack, onPlanWithAi, onDelete = { confirmDelete = true })
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    TwOverlayIconButton(TwIcons.Back, stringResource(R.string.feature_trips_back), onBack, modifier.padding(TripWiseTheme.spacing.sm))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Ready(
    state: TripDetailUiState.Ready,
    onAction: (TripDetailAction) -> Unit,
    onBack: () -> Unit,
    onPlanWithAi: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val trip = state.trip
    val locale = currentLocale
    val tabs = listOf(
        stringResource(R.string.feature_trips_tab_overview),
        stringResource(R.string.feature_trips_tab_itinerary),
        stringResource(R.string.feature_trips_tab_budget),
    )
    val pager = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().height(220.dp)) {
            RemoteImage(trip.coverImage, contentDescription = null, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(TripWiseTheme.extendedColors.imageScrim))
            Row(Modifier.fillMaxWidth().statusBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
                BackButton(onBack)
                Box(Modifier.weight(1f))
                TwOverlayIconButton(TwIcons.Delete, stringResource(R.string.feature_trips_delete), onDelete,
                    Modifier.padding(TripWiseTheme.spacing.sm))
            }
            Column(Modifier.align(Alignment.BottomStart).padding(TripWiseTheme.spacing.lg)) {
                TripStatusPill(trip, state.today)
                Text(
                    trip.title,
                    style = TripWiseTheme.typography.headlineMedium,
                    color = TripWiseTheme.extendedColors.onImage,
                    modifier = Modifier.padding(top = TripWiseTheme.spacing.sm).semantics { heading() },
                )
                Text(
                    "${trip.destinationName} · ${Formatters.dateRange(trip.startDate, trip.endDate, locale, state.today)}",
                    style = TripWiseTheme.typography.bodyMedium,
                    color = TripWiseTheme.extendedColors.onImage,
                )
            }
        }
        PrimaryTabRow(selectedTabIndex = pager.currentPage, containerColor = TripWiseTheme.colors.surface) {
            tabs.forEachIndexed { i, title ->
                Tab(
                    selected = pager.currentPage == i,
                    onClick = { scope.launch { pager.animateScrollToPage(i) } },
                    text = { Text(title) },
                )
            }
        }
        HorizontalPager(pager, Modifier.weight(1f)) { page ->
            when (page) {
                0 -> OverviewTab(state) { onPlanWithAi(trip.id) }
                1 -> ItineraryTab(state, onAction) { onPlanWithAi(trip.id) }
                else -> BudgetTab(state)
            }
        }
    }
}

@Composable
private fun OverviewTab(state: TripDetailUiState.Ready, onPlanWithAi: () -> Unit) {
    val trip = state.trip
    val locale = currentLocale
    LazyColumn(
        contentPadding = PaddingValues(TripWiseTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.lg),
        modifier = Modifier.navigationBarsPadding(),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md)) {
                TwStatTile(trip.dayCount.toString(), stringResource(R.string.feature_trips_days_label), Modifier.weight(1f), TwIcons.Calendar)
                TwStatTile(trip.travellers.toString(), stringResource(R.string.feature_trips_travellers_label), Modifier.weight(1f), TwIcons.Travellers)
                TwStatTile(
                    trip.budget?.let { Formatters.money(it, locale) } ?: "—",
                    stringResource(R.string.feature_trips_budget_label), Modifier.weight(1.4f), TwIcons.Wallet,
                )
            }
        }
        state.nextItem?.let { next ->
            item {
                TwCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.feature_trips_next_up), style = TripWiseTheme.typography.labelLarge, color = TripWiseTheme.colors.secondary)
                    Text(next.title, style = TripWiseTheme.typography.titleMedium, modifier = Modifier.padding(top = TripWiseTheme.spacing.xs))
                    Text(
                        listOfNotNull(Formatters.dayLabel(next.date, locale), next.startTime?.let { Formatters.time(it, locale) }).joinToString(" · "),
                        style = TripWiseTheme.typography.bodyMedium,
                        color = TripWiseTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
        trip.notes?.let { notes ->
            item {
                TwCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.feature_trips_notes), style = TripWiseTheme.typography.labelLarge, color = TripWiseTheme.colors.secondary)
                    Text(notes, style = TripWiseTheme.typography.bodyLarge, modifier = Modifier.padding(top = TripWiseTheme.spacing.xs))
                }
            }
        }
        item {
            TwOutlinedButton(
                stringResource(R.string.feature_trips_generate_ai), onPlanWithAi,
                Modifier.fillMaxWidth(), leadingIcon = TwIcons.Assistant,
            )
        }
    }
}

@Composable
private fun ItineraryTab(state: TripDetailUiState.Ready, onAction: (TripDetailAction) -> Unit, onPlanWithAi: () -> Unit) {
    var selectedDay by rememberSaveable { mutableIntStateOf(0) }
    val day = state.days.getOrNull(selectedDay) ?: state.days.firstOrNull()
    val locale = currentLocale
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(TripWiseTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
        ) {
            state.days.forEachIndexed { i, d ->
                TwFilterChip(stringResource(R.string.feature_trips_day, d.index), i == selectedDay, { selectedDay = i })
            }
        }
        if (day != null) {
            Text(
                Formatters.dayLabel(day.date, locale),
                style = TripWiseTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = TripWiseTheme.spacing.lg),
            )
        }
        LazyColumn(
            contentPadding = PaddingValues(TripWiseTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
            modifier = Modifier.weight(1f).navigationBarsPadding(),
        ) {
            val items = day?.items.orEmpty()
            if (items.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.feature_trips_day_empty),
                        style = TripWiseTheme.typography.bodyMedium,
                        color = TripWiseTheme.colors.onSurfaceVariant,
                    )
                }
            }
            items(items, key = { it.id }) { item ->
                ItineraryRow(item) { onAction(TripDetailAction.SetItemDone(item.id, it)) }
            }
            item {
                Column(Modifier.padding(top = TripWiseTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm)) {
                    TwButton(stringResource(R.string.feature_trips_generate_ai), onPlanWithAi, Modifier.fillMaxWidth(), leadingIcon = TwIcons.Assistant)
                    TwOutlinedButton(stringResource(R.string.feature_trips_add_activity), {}, Modifier.fillMaxWidth(), enabled = false, leadingIcon = TwIcons.Add)
                    LaterPhaseCaption()
                }
            }
        }
    }
}

@Composable
private fun ItineraryRow(item: ItineraryItem, onDoneChange: (Boolean) -> Unit) {
    val locale = currentLocale
    val doneLabel = stringResource(R.string.feature_trips_mark_done, item.title)
    Surface(
        shape = TripWiseTheme.shapes.card,
        color = TripWiseTheme.colors.surfaceContainerLowest,
        modifier = Modifier.toggleable(value = item.isDone, role = Role.Checkbox, onValueChange = onDoneChange)
            .semantics(mergeDescendants = true) {},
    ) {
        Row(Modifier.padding(TripWiseTheme.spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Text(
                item.startTime?.let { Formatters.time(it, locale) } ?: "",
                style = TripWiseTheme.typography.labelLarge,
                color = TripWiseTheme.colors.secondary,
                modifier = Modifier.width(64.dp),
            )
            Icon(item.category.ui.icon, contentDescription = stringResource(item.category.ui.label), Modifier.size(20.dp), tint = TripWiseTheme.colors.onSurfaceVariant)
            Column(Modifier.weight(1f).padding(horizontal = TripWiseTheme.spacing.md)) {
                Text(
                    item.title,
                    style = TripWiseTheme.typography.bodyLarge,
                    textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    item.estimatedCost?.let { if (it.amountMinor == 0L) stringResource(R.string.feature_trips_free) else Formatters.money(it, locale) } ?: "",
                    style = TripWiseTheme.typography.bodySmall,
                    color = TripWiseTheme.colors.onSurfaceVariant,
                )
            }
            // Row is the toggle target; checkbox is visual only (null onCheckedChange) to avoid a double tap target.
            Checkbox(checked = item.isDone, onCheckedChange = null, modifier = Modifier.semantics { this.contentDescription = doneLabel })
        }
    }
}

@Composable
private fun BudgetTab(state: TripDetailUiState.Ready) {
    val budget = state.budget
    val locale = currentLocale
    LazyColumn(
        contentPadding = PaddingValues(TripWiseTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
        modifier = Modifier.navigationBarsPadding(),
    ) {
        if (budget == null) {
            item { Text(stringResource(R.string.feature_trips_no_budget), style = TripWiseTheme.typography.bodyLarge) }
        } else {
            item {
                TwCard(Modifier.fillMaxWidth()) {
                    Text(Formatters.money(budget.spent, locale), style = TripWiseTheme.typography.headlineMedium)
                    BudgetProgress(budget, Modifier.padding(top = TripWiseTheme.spacing.sm))
                }
            }
            item { TwSectionHeader(stringResource(R.string.feature_trips_tab_budget), Modifier.padding(top = TripWiseTheme.spacing.sm)) }
            items(budget.categories, key = { it.category.name }) { CategoryRow(it) }
        }
        item {
            Column(Modifier.padding(top = TripWiseTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm)) {
                TwOutlinedButton(stringResource(R.string.feature_trips_add_expense), {}, Modifier.fillMaxWidth(), enabled = false, leadingIcon = TwIcons.Add)
                LaterPhaseCaption()
            }
        }
    }
}

@Composable
private fun CategoryRow(c: CategoryBudget) {
    val locale = currentLocale
    val fraction = if (c.planned.amountMinor <= 0) 0f else c.spent.amountMinor.toFloat() / c.planned.amountMinor
    val label = stringResource(c.category.ui.label)
    val amounts = stringResource(R.string.feature_trips_planned_spent, Formatters.money(c.spent, locale), Formatters.money(c.planned, locale))
    Column(Modifier.semantics(mergeDescendants = true) {}) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(c.category.ui.icon, contentDescription = null, Modifier.size(20.dp), tint = TripWiseTheme.colors.secondary)
            Text(label, style = TripWiseTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(start = TripWiseTheme.spacing.md))
            Text(
                amounts,
                style = TripWiseTheme.typography.bodyMedium,
                color = if (fraction > 1f) TripWiseTheme.colors.error else TripWiseTheme.colors.onSurfaceVariant,
            )
        }
        TwProgressBar(fraction, "$label: $amounts", Modifier.padding(top = TripWiseTheme.spacing.sm))
    }
}

@Composable
private fun LaterPhaseCaption() {
    Text(
        stringResource(R.string.feature_trips_later_phase),
        style = TripWiseTheme.typography.bodySmall,
        color = TripWiseTheme.colors.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}

@PreviewLightDark
@Composable
private fun TripDetailPreview() = TripWiseTheme {
    val trip = PreviewData.trip
    TripDetailScreen(
        TripDetailUiState.Ready(
            trip, PreviewData.today,
            (0 until trip.dayCount).map { ItineraryDay(it + 1, trip.startDate.plusDays(it.toLong()), if (it == 0) PreviewData.itinerary else emptyList()) },
            PreviewData.budget, PreviewData.itinerary[1],
        ),
        {}, {}, {},
    )
}
