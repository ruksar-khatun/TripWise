package com.tripwise.feature.trips.budget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.designsystem.component.TwButton
import com.tripwise.core.designsystem.component.TwCard
import com.tripwise.core.designsystem.component.TwFilterChip
import com.tripwise.core.designsystem.component.TwOutlinedButton
import com.tripwise.core.designsystem.component.TwProgressBar
import com.tripwise.core.designsystem.component.TwSectionHeader
import com.tripwise.core.designsystem.component.TwSegmentedButtons
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.model.BudgetLevel
import com.tripwise.core.model.CategoryBudget
import com.tripwise.core.model.ExpenseCategory
import com.tripwise.core.ui.component.BudgetProgress
import com.tripwise.core.ui.error.messageRes
import com.tripwise.core.ui.format.Formatters
import com.tripwise.core.ui.format.currentLocale
import com.tripwise.core.ui.label.ui
import com.tripwise.feature.trips.R

/** Budget tab of Trip Detail: estimate, planned amounts per category, and expenses. */
@Composable
internal fun BudgetTab(viewModel: BudgetViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BudgetTab(state, viewModel::onAction)
}

@Composable
internal fun BudgetTab(state: BudgetUiState, onAction: (BudgetAction) -> Unit) {
    val locale = currentLocale
    LazyColumn(
        contentPadding = PaddingValues(TripWiseTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
        modifier = Modifier.navigationBarsPadding(),
    ) {
        item {
            TwCard(Modifier.fillMaxWidth()) {
                val budget = state.budget
                if (budget != null) {
                    Text(Formatters.money(budget.spent, locale), style = TripWiseTheme.typography.headlineMedium)
                    BudgetProgress(budget, Modifier.padding(top = TripWiseTheme.spacing.sm))
                } else {
                    Text(stringResource(R.string.feature_trips_no_budget), style = TripWiseTheme.typography.bodyLarge)
                }
            }
        }
        item { EstimateCard(state, onAction) }
        val categories = state.budget?.categories.orEmpty()
        if (categories.isNotEmpty()) {
            item { TwSectionHeader(stringResource(R.string.feature_trips_by_category), Modifier.padding(top = TripWiseTheme.spacing.sm)) }
            items(categories, key = { "c-" + it.category.name }) { c -> CategoryRow(c) { onAction(BudgetAction.EditPlanned(c.category)) } }
        }
        item {
            TwSectionHeader(
                stringResource(R.string.feature_trips_expenses), Modifier.padding(top = TripWiseTheme.spacing.sm),
                actionLabel = stringResource(R.string.feature_trips_add_expense), onAction = { onAction(BudgetAction.AddExpense) },
            )
        }
        if (state.expenses.isEmpty()) {
            item { Text(stringResource(R.string.feature_trips_no_expenses), style = TripWiseTheme.typography.bodyMedium, color = TripWiseTheme.colors.onSurfaceVariant) }
        }
        items(state.expenses, key = { it.id }) { e ->
            Surface(
                Modifier.fillMaxWidth().clickable { onAction(BudgetAction.EditExpense(e)) },
                shape = TripWiseTheme.shapes.card, color = TripWiseTheme.colors.surfaceContainerLowest,
            ) {
                Row(Modifier.padding(TripWiseTheme.spacing.md).semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically) {
                    Icon(e.category.ui.icon, contentDescription = null, tint = TripWiseTheme.colors.secondary)
                    Column(Modifier.weight(1f).padding(horizontal = TripWiseTheme.spacing.md)) {
                        Text(e.note ?: stringResource(e.category.ui.label), style = TripWiseTheme.typography.bodyLarge)
                        Text("${stringResource(e.category.ui.label)} · ${Formatters.dayLabel(e.spentOn, locale)}",
                            style = TripWiseTheme.typography.bodySmall, color = TripWiseTheme.colors.onSurfaceVariant)
                    }
                    Text(Formatters.money(e.amount, locale), style = TripWiseTheme.typography.titleSmall)
                }
            }
        }
        item { TwButton(stringResource(R.string.feature_trips_add_expense), { onAction(BudgetAction.AddExpense) }, Modifier.fillMaxWidth(), leadingIcon = TwIcons.Add) }
    }
    state.expenseEditor?.let { ExpenseDialog(it, state, onAction) }
    state.plannedEditor?.let { PlannedDialog(it, state, onAction) }
}

@Composable
private fun EstimateCard(state: BudgetUiState, onAction: (BudgetAction) -> Unit) {
    val locale = currentLocale
    TwCard(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.feature_trips_estimate_title), style = TripWiseTheme.typography.titleMedium)
        Text(stringResource(R.string.feature_trips_estimate_body), style = TripWiseTheme.typography.bodySmall, color = TripWiseTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(bottom = TripWiseTheme.spacing.sm))
        TwSegmentedButtons(
            listOf(R.string.feature_trips_level_budget, R.string.feature_trips_level_mid, R.string.feature_trips_level_luxury).map { stringResource(it) },
            state.level.ordinal, { onAction(BudgetAction.SetLevel(BudgetLevel.entries[it])) },
        )
        when (val e = state.estimate) {
            null -> TwOutlinedButton(stringResource(R.string.feature_trips_estimate_action), { onAction(BudgetAction.Estimate) },
                Modifier.fillMaxWidth().padding(top = TripWiseTheme.spacing.md), leadingIcon = TwIcons.Wallet)
            LoadState.Loading -> CircularProgressIndicator(Modifier.padding(TripWiseTheme.spacing.md).size(24.dp))
            is LoadState.Failed -> Text(stringResource(e.error.messageRes()), color = TripWiseTheme.colors.error, modifier = Modifier.padding(top = TripWiseTheme.spacing.md))
            is LoadState.Ready -> Column(Modifier.padding(top = TripWiseTheme.spacing.md)) {
                e.data.perCategory.entries.sortedBy { it.key.ordinal }.forEach { (c, m) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text(stringResource(c.ui.label), style = TripWiseTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(Formatters.money(m, locale), style = TripWiseTheme.typography.bodyMedium)
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = TripWiseTheme.spacing.sm)) {
                    Text(stringResource(R.string.feature_trips_estimate_total), style = TripWiseTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Text(Formatters.money(e.data.total, locale), style = TripWiseTheme.typography.titleSmall)
                }
                Text(
                    stringResource(if (e.data.liveRates) R.string.feature_trips_estimate_note else R.string.feature_trips_estimate_note_offline_rates, e.data.days, e.data.travellers),
                    style = TripWiseTheme.typography.bodySmall, color = TripWiseTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = TripWiseTheme.spacing.sm),
                )
                TwButton(stringResource(R.string.feature_trips_estimate_apply), { onAction(BudgetAction.ApplyEstimate) }, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun CategoryRow(c: CategoryBudget, onEdit: () -> Unit) {
    val locale = currentLocale
    val fraction = if (c.planned.amountMinor <= 0) 0f else c.spent.amountMinor.toFloat() / c.planned.amountMinor
    val label = stringResource(c.category.ui.label)
    val amounts = stringResource(R.string.feature_trips_planned_spent, Formatters.money(c.spent, locale), Formatters.money(c.planned, locale))
    Column(Modifier.fillMaxWidth().clickable(onClickLabel = stringResource(R.string.feature_trips_edit_planned), onClick = onEdit).padding(vertical = TripWiseTheme.spacing.xs)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(c.category.ui.icon, contentDescription = null, Modifier.size(20.dp), tint = TripWiseTheme.colors.secondary)
            Text(label, style = TripWiseTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(start = TripWiseTheme.spacing.md))
            Text(amounts, style = TripWiseTheme.typography.bodyMedium, color = if (fraction > 1f) TripWiseTheme.colors.error else TripWiseTheme.colors.onSurfaceVariant)
        }
        TwProgressBar(fraction, "$label: $amounts", Modifier.padding(top = TripWiseTheme.spacing.sm))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpenseDialog(editor: ExpenseEditor, state: BudgetUiState, onAction: (BudgetAction) -> Unit) {
    val invalid = editor.showErrors && BudgetViewModel.parseAmount(editor.amount, state.currency) == null
    AlertDialog(
        onDismissRequest = { onAction(BudgetAction.DismissDialog) },
        title = { Text(stringResource(if (editor.id == null) R.string.feature_trips_add_expense else R.string.feature_trips_edit_expense)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.xs), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.xs)) {
                    ExpenseCategory.entries.forEach { c ->
                        TwFilterChip(stringResource(c.ui.label), editor.category == c, { onAction(BudgetAction.UpdateExpenseEditor(editor.copy(category = c))) })
                    }
                }
                OutlinedTextField(
                    value = editor.amount, onValueChange = { onAction(BudgetAction.UpdateExpenseEditor(editor.copy(amount = it.take(15)))) },
                    label = { Text(stringResource(R.string.feature_trips_amount, state.currency.code)) },
                    isError = invalid, supportingText = if (invalid) { { Text(stringResource(R.string.feature_trips_error_amount)) } } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
                )
                OutlinedTextField(
                    value = editor.note, onValueChange = { onAction(BudgetAction.UpdateExpenseEditor(editor.copy(note = it.take(120)))) },
                    label = { Text(stringResource(R.string.feature_trips_note)) }, singleLine = true,
                )
                state.error?.let { Text(stringResource(it.messageRes()), color = TripWiseTheme.colors.error) }
            }
        },
        confirmButton = { TextButton(onClick = { onAction(BudgetAction.SaveExpense) }) { Text(stringResource(R.string.feature_trips_save_short)) } },
        dismissButton = {
            Row {
                if (editor.id != null) {
                    TextButton(onClick = { onAction(BudgetAction.DeleteExpense) }) { Text(stringResource(R.string.feature_trips_delete_short), color = TripWiseTheme.colors.error) }
                }
                TextButton(onClick = { onAction(BudgetAction.DismissDialog) }) { Text(stringResource(R.string.feature_trips_cancel)) }
            }
        },
    )
}

@Composable
private fun PlannedDialog(editor: PlannedEditor, state: BudgetUiState, onAction: (BudgetAction) -> Unit) {
    val invalid = editor.showErrors && BudgetViewModel.parseAmount(editor.amount, state.currency, allowZero = true) == null
    AlertDialog(
        onDismissRequest = { onAction(BudgetAction.DismissDialog) },
        title = { Text(stringResource(R.string.feature_trips_planned_for, stringResource(editor.category.ui.label))) },
        text = {
            OutlinedTextField(
                value = editor.amount, onValueChange = { onAction(BudgetAction.UpdatePlannedEditor(it.take(15))) },
                label = { Text(stringResource(R.string.feature_trips_amount, state.currency.code)) },
                isError = invalid, supportingText = if (invalid) { { Text(stringResource(R.string.feature_trips_error_amount)) } } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
            )
        },
        confirmButton = { TextButton(onClick = { onAction(BudgetAction.SavePlanned) }) { Text(stringResource(R.string.feature_trips_save_short)) } },
        dismissButton = { TextButton(onClick = { onAction(BudgetAction.DismissDialog) }) { Text(stringResource(R.string.feature_trips_cancel)) } },
    )
}
