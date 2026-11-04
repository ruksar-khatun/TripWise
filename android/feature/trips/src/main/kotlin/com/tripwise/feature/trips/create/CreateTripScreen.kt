package com.tripwise.feature.trips.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.designsystem.component.TwButton
import com.tripwise.core.designsystem.component.TwTextField
import com.tripwise.core.designsystem.component.TwTopAppBar
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.ui.format.Formatters
import com.tripwise.core.ui.format.currentLocale
import com.tripwise.feature.trips.R
import com.tripwise.feature.trips.create.CreateTripUiState.Companion.FIELD_BUDGET
import com.tripwise.feature.trips.create.CreateTripUiState.Companion.FIELD_DATES
import com.tripwise.feature.trips.create.CreateTripUiState.Companion.FIELD_DESTINATION
import com.tripwise.feature.trips.create.CreateTripUiState.Companion.FIELD_TITLE
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
internal fun CreateTripScreen(onClose: () -> Unit, viewModel: CreateTripViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CreateTripScreen(state, viewModel::onAction, onClose)
}

@Composable
private fun FieldError?.message(): String? = when (this) {
    null -> null
    FieldError.REQUIRED -> stringResource(R.string.feature_trips_error_required)
    FieldError.TRAVELLERS_RANGE -> stringResource(R.string.feature_trips_error_travellers)
    FieldError.BUDGET_FORMAT -> stringResource(R.string.feature_trips_error_budget)
}

@Composable
internal fun CreateTripScreen(state: CreateTripUiState, onAction: (CreateTripAction) -> Unit, onClose: () -> Unit) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val locale = currentLocale
    Scaffold(
        topBar = {
            TwTopAppBar(
                title = stringResource(R.string.feature_trips_create_title),
                onNavigateBack = onClose,
                navigateBackContentDescription = stringResource(R.string.feature_trips_close),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(TripWiseTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val fieldModifier = Modifier.fillMaxWidth().widthIn(max = 560.dp)
            TwTextField(
                state.title, { onAction(CreateTripAction.TitleChanged(it)) }, stringResource(R.string.feature_trips_field_title),
                fieldModifier, error = state.visibleError(FIELD_TITLE).message(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            TwTextField(
                state.destination, { onAction(CreateTripAction.DestinationChanged(it)) }, stringResource(R.string.feature_trips_field_destination),
                fieldModifier, error = state.visibleError(FIELD_DESTINATION).message(), leadingIcon = TwIcons.Place,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )
            // Read-only field that opens the date range picker.
            Box(fieldModifier) {
                TwTextField(
                    value = if (state.startDate != null && state.endDate != null) {
                        Formatters.dateRange(state.startDate, state.endDate, locale)
                    } else {
                        ""
                    },
                    onValueChange = {},
                    label = stringResource(R.string.feature_trips_field_dates),
                    placeholder = stringResource(R.string.feature_trips_field_dates_placeholder),
                    leadingIcon = TwIcons.Calendar,
                    error = state.visibleError(FIELD_DATES).message(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(
                    Modifier.matchParentSize().clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClickLabel = stringResource(R.string.feature_trips_field_dates_placeholder),
                    ) { showDatePicker = true },
                )
            }
            TravellersStepper(state.travellers, { onAction(CreateTripAction.TravellersChanged(it)) }, fieldModifier)
            TwTextField(
                state.budget, { onAction(CreateTripAction.BudgetChanged(it)) },
                stringResource(R.string.feature_trips_field_budget, state.currency.code), fieldModifier,
                error = state.visibleError(FIELD_BUDGET).message(), leadingIcon = TwIcons.Wallet,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            TwTextField(
                state.notes, { onAction(CreateTripAction.NotesChanged(it)) }, stringResource(R.string.feature_trips_field_notes),
                fieldModifier, singleLine = false, leadingIcon = TwIcons.Notes,
            )
            TwButton(stringResource(R.string.feature_trips_save), { onAction(CreateTripAction.Submit) }, fieldModifier.padding(top = TripWiseTheme.spacing.sm))
            if (state.showErrors && state.isValid) {
                Text(
                    stringResource(R.string.feature_trips_save_later),
                    style = TripWiseTheme.typography.bodyMedium,
                    color = TripWiseTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = fieldModifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }
    }
    if (showDatePicker) {
        DateRangeDialog(state.startDate, state.endDate, onDismiss = { showDatePicker = false }) { start, end ->
            onAction(CreateTripAction.DatesChanged(start, end))
            showDatePicker = false
        }
    }
}

@Composable
private fun TravellersStepper(value: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.padding(vertical = TripWiseTheme.spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        Icon(TwIcons.Travellers, contentDescription = null, tint = TripWiseTheme.colors.onSurfaceVariant)
        Text(
            stringResource(R.string.feature_trips_field_travellers),
            style = TripWiseTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f).padding(start = TripWiseTheme.spacing.md),
        )
        FilledTonalIconButton(onClick = { onChange(value - 1) }, enabled = value > 1) {
            Icon(TwIcons.Remove, contentDescription = stringResource(R.string.feature_trips_decrease))
        }
        Text(
            value.toString(),
            style = TripWiseTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 40.dp).semantics { liveRegion = LiveRegionMode.Polite },
        )
        FilledTonalIconButton(onClick = { onChange(value + 1) }, enabled = value < CreateTripUiState.MAX_TRAVELLERS) {
            Icon(TwIcons.Add, contentDescription = stringResource(R.string.feature_trips_increase))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeDialog(
    start: LocalDate?,
    end: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate?, LocalDate?) -> Unit,
) {
    fun LocalDate.millis() = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    fun Long.date() = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
    val pickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = start?.millis(),
        initialSelectedEndDateMillis = end?.millis(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(pickerState.selectedStartDateMillis?.date(), pickerState.selectedEndDateMillis?.date()) },
                enabled = pickerState.selectedEndDateMillis != null,
            ) { Text(stringResource(R.string.feature_trips_ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.feature_trips_cancel)) } },
    ) {
        DateRangePicker(state = pickerState, modifier = Modifier.weight(1f))
    }
}

@PreviewLightDark
@Composable
private fun CreateTripPreview() = TripWiseTheme {
    CreateTripScreen(CreateTripUiState(title = "Kyoto in Spring", showErrors = true), {}, {})
}
