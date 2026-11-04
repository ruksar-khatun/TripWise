package com.tripwise.feature.itinerary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.designsystem.component.TwButton
import com.tripwise.core.designsystem.component.TwCard
import com.tripwise.core.designsystem.component.TwFilterChip
import com.tripwise.core.designsystem.component.TwOutlinedButton
import com.tripwise.core.designsystem.component.TwSegmentedButtons
import com.tripwise.core.designsystem.component.TwTopAppBar
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.model.BudgetLevel
import com.tripwise.core.model.ItineraryPlan
import com.tripwise.core.model.PlanItem
import com.tripwise.core.model.PlanSource
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.model.TravelPace
import com.tripwise.core.ui.component.costLabel
import com.tripwise.core.ui.component.durationLabel
import com.tripwise.core.ui.error.messageRes
import com.tripwise.core.ui.format.Formatters
import com.tripwise.core.ui.format.currentLocale
import com.tripwise.core.ui.label.ui
import com.tripwise.core.ui.state.ErrorState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
internal fun PlannerScreen(onBack: () -> Unit, onSaved: (tripId: String) -> Unit, viewModel: PlannerViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.savedTripId) { state.savedTripId?.let(onSaved) }
    PlannerScreen(state, viewModel::onAction, onBack)
}

@Composable
internal fun PlannerScreen(state: PlannerUiState, onAction: (PlannerAction) -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TwTopAppBar(
                title = stringResource(R.string.feature_itinerary_title),
                onNavigateBack = onBack,
                navigateBackContentDescription = stringResource(R.string.feature_itinerary_back),
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.widthIn(max = 720.dp).fillMaxSize()) {
                when (val plan = state.plan) {
                    null -> Preferences(state, onAction)
                    LoadState.Loading -> Generating()
                    is LoadState.Failed -> Column {
                        ErrorState(plan.error, onRetry = { onAction(PlannerAction.Generate) })
                    }
                    is LoadState.Ready -> PlanView(plan.data, state, onAction)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun Preferences(state: PlannerUiState, onAction: (PlannerAction) -> Unit) {
    var pickDate by rememberSaveable { mutableStateOf(false) }
    LazyColumn(contentPadding = PaddingValues(TripWiseTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.lg)) {
        state.trip?.let { trip ->
            item { Text(stringResource(R.string.feature_itinerary_for_trip, trip.title), style = TripWiseTheme.typography.titleMedium, color = TripWiseTheme.colors.secondary) }
        }
        item { DestinationPicker(state, onAction) }
        item {
            Stepper(stringResource(R.string.feature_itinerary_days), state.days, { onAction(PlannerAction.SetDays(it)) },
                PlannerViewModel.MIN_DAYS, PlannerViewModel.MAX_DAYS, enabled = state.trip == null)
        }
        item { Stepper(stringResource(R.string.feature_itinerary_travellers), state.travellers, { onAction(PlannerAction.SetTravellers(it)) }, 1, 20) }
        if (state.trip == null) {
            item {
                OutlinedTextField(
                    value = Formatters.dayLabel(state.startDate, currentLocale), onValueChange = {}, readOnly = true,
                    label = { Text(stringResource(R.string.feature_itinerary_start_date)) },
                    leadingIcon = { Icon(TwIcons.Calendar, null) },
                    trailingIcon = { TextButton(onClick = { pickDate = true }) { Text(stringResource(R.string.feature_itinerary_change)) } },
                    modifier = Modifier.fillMaxWidth(), shape = TripWiseTheme.shapes.field,
                )
            }
        }
        item {
            Label(stringResource(R.string.feature_itinerary_pace))
            TwSegmentedButtons(
                listOf(R.string.feature_itinerary_pace_relaxed, R.string.feature_itinerary_pace_balanced, R.string.feature_itinerary_pace_packed).map { stringResource(it) },
                state.pace.ordinal, { onAction(PlannerAction.SetPace(TravelPace.entries[it])) },
            )
        }
        item {
            Label(stringResource(R.string.feature_itinerary_budget))
            TwSegmentedButtons(
                listOf(R.string.feature_itinerary_budget_low, R.string.feature_itinerary_budget_mid, R.string.feature_itinerary_budget_luxury).map { stringResource(it) },
                state.budgetLevel.ordinal, { onAction(PlannerAction.SetBudget(BudgetLevel.entries[it])) },
            )
        }
        item {
            Label(stringResource(R.string.feature_itinerary_interests))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm)) {
                TravelInterest.entries.forEach { i ->
                    TwFilterChip(stringResource(i.ui.label), i in state.interests, { onAction(PlannerAction.ToggleInterest(i)) }, leadingIcon = i.ui.icon)
                }
            }
        }
        item {
            OutlinedTextField(
                value = state.notes, onValueChange = { onAction(PlannerAction.SetNotes(it)) },
                label = { Text(stringResource(R.string.feature_itinerary_notes)) },
                placeholder = { Text(stringResource(R.string.feature_itinerary_notes_hint)) },
                modifier = Modifier.fillMaxWidth(), minLines = 2, shape = TripWiseTheme.shapes.field,
            )
        }
        item {
            TwButton(
                stringResource(R.string.feature_itinerary_generate), { onAction(PlannerAction.Generate) },
                Modifier.fillMaxWidth().navigationBarsPadding(), enabled = state.canGenerate, leadingIcon = TwIcons.Assistant,
            )
        }
    }
    if (pickDate) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = state.startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(onClick = {
                    picker.selectedDateMillis?.let { onAction(PlannerAction.SetStartDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())) }
                    pickDate = false
                }) { Text(stringResource(R.string.feature_itinerary_ok)) }
            },
        ) { DatePicker(picker) }
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = TripWiseTheme.typography.titleSmall, modifier = Modifier.padding(bottom = TripWiseTheme.spacing.sm).semantics { heading() })
}

@Composable
private fun DestinationPicker(state: PlannerUiState, onAction: (PlannerAction) -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = state.destination?.let { "${it.name}, ${it.country}" } ?: "",
            onValueChange = {}, readOnly = true, enabled = state.trip == null || state.destinationId == null,
            label = { Text(stringResource(R.string.feature_itinerary_destination)) },
            leadingIcon = { Icon(TwIcons.Place, null) },
            modifier = Modifier.fillMaxWidth(), shape = TripWiseTheme.shapes.field,
        )
        if (state.trip == null || state.destinationId == null) {
            Box(Modifier.matchParentSize().padding(top = 8.dp).clickable(onClickLabel = stringResource(R.string.feature_itinerary_destination)) { open = true })
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            state.destinations.forEach { d ->
                DropdownMenuItem(text = { Text("${d.name}, ${d.country}") }, onClick = { onAction(PlannerAction.SelectDestination(d.id)); open = false })
            }
        }
    }
}

@Composable
private fun Stepper(label: String, value: Int, onChange: (Int) -> Unit, min: Int, max: Int, enabled: Boolean = true) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = TripWiseTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        FilledTonalIconButton(onClick = { onChange(value - 1) }, enabled = enabled && value > min) {
            Icon(TwIcons.Remove, stringResource(R.string.feature_itinerary_decrease, label))
        }
        Text(value.toString(), style = TripWiseTheme.typography.titleLarge, textAlign = TextAlign.Center,
            modifier = Modifier.width(48.dp).semantics { liveRegion = LiveRegionMode.Polite })
        FilledTonalIconButton(onClick = { onChange(value + 1) }, enabled = enabled && value < max) {
            Icon(TwIcons.Add, stringResource(R.string.feature_itinerary_increase, label))
        }
    }
}

@Composable
private fun Generating() {
    Column(
        Modifier.fillMaxSize().semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.lg, Alignment.CenterVertically),
    ) {
        CircularProgressIndicator()
        Text(stringResource(R.string.feature_itinerary_generating), style = TripWiseTheme.typography.titleMedium)
    }
}

@Composable
private fun PlanView(plan: ItineraryPlan, state: PlannerUiState, onAction: (PlannerAction) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(TripWiseTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md)) {
        item {
            Surface(
                shape = TripWiseTheme.shapes.field,
                color = if (plan.source == PlanSource.AI) TripWiseTheme.colors.secondaryContainer else TripWiseTheme.colors.surfaceContainerHigh,
            ) {
                Row(Modifier.padding(TripWiseTheme.spacing.md), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (plan.source == PlanSource.AI) TwIcons.Assistant else TwIcons.Info, null)
                    Text(
                        stringResource(if (plan.source == PlanSource.AI) R.string.feature_itinerary_source_ai else R.string.feature_itinerary_source_template),
                        style = TripWiseTheme.typography.bodyMedium, modifier = Modifier.padding(start = TripWiseTheme.spacing.md),
                    )
                }
            }
        }
        item {
            Text(plan.title, style = TripWiseTheme.typography.headlineSmall, color = TripWiseTheme.colors.primary, modifier = Modifier.semantics { heading() })
            Text(plan.summary, style = TripWiseTheme.typography.bodyMedium, color = TripWiseTheme.colors.onSurfaceVariant)
        }
        plan.days.forEach { day ->
            item(key = "day-${day.day}") {
                TwCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.feature_itinerary_day, day.day), style = TripWiseTheme.typography.labelLarge, color = TripWiseTheme.colors.secondary)
                    Text(day.theme, style = TripWiseTheme.typography.titleMedium, modifier = Modifier.padding(bottom = TripWiseTheme.spacing.sm).semantics { heading() })
                    day.items.forEach { PlanItemRow(it) }
                }
            }
        }
        item {
            Text(stringResource(R.string.feature_itinerary_estimate_note), style = TripWiseTheme.typography.bodySmall, color = TripWiseTheme.colors.onSurfaceVariant)
        }
        state.saveError?.let { item { Text(stringResource(it.messageRes()), color = TripWiseTheme.colors.error) } }
        item {
            Column(Modifier.navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm)) {
                TwButton(
                    stringResource(if (state.trip != null) R.string.feature_itinerary_save_to_trip else R.string.feature_itinerary_save_new_trip),
                    { onAction(PlannerAction.Save) }, Modifier.fillMaxWidth(), loading = state.saving, leadingIcon = TwIcons.Check,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm)) {
                    TwOutlinedButton(stringResource(R.string.feature_itinerary_regenerate), { onAction(PlannerAction.Generate) }, Modifier.weight(1f), leadingIcon = TwIcons.Refresh)
                    TwOutlinedButton(stringResource(R.string.feature_itinerary_edit_preferences), { onAction(PlannerAction.EditPreferences) }, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PlanItemRow(item: PlanItem) {
    val locale = currentLocale
    Row(Modifier.fillMaxWidth().padding(vertical = TripWiseTheme.spacing.sm).semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.Top) {
        Text(Formatters.time(item.startTime, locale), style = TripWiseTheme.typography.labelLarge, color = TripWiseTheme.colors.secondary, modifier = Modifier.width(64.dp))
        Icon(item.category.ui.icon, contentDescription = stringResource(item.category.ui.label), Modifier.size(20.dp), tint = TripWiseTheme.colors.onSurfaceVariant)
        Column(Modifier.weight(1f).padding(start = TripWiseTheme.spacing.md)) {
            Text(item.title, style = TripWiseTheme.typography.bodyLarge)
            Text("${durationLabel(item.durationMinutes)} · ${costLabel(item.estimatedCost)}", style = TripWiseTheme.typography.bodySmall, color = TripWiseTheme.colors.onSurfaceVariant)
            item.notes?.takeIf { it.isNotBlank() }?.let { Text(it, style = TripWiseTheme.typography.bodySmall, color = TripWiseTheme.colors.onSurfaceVariant) }
        }
    }
}

@PreviewLightDark
@Composable
private fun PlannerPreview() = TripWiseTheme {
    PlannerScreen(PlannerUiState(startDate = LocalDate.of(2026, 4, 1)), {}, {})
}
