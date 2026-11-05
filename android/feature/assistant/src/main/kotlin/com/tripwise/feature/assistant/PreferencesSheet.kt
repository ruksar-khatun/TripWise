package com.tripwise.feature.assistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tripwise.core.designsystem.component.TwButton
import com.tripwise.core.designsystem.component.TwFilterChip
import com.tripwise.core.designsystem.component.TwOutlinedButton
import com.tripwise.core.designsystem.component.TwSegmentedButtons
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.model.BudgetLevel
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.model.TripPreferences
import com.tripwise.core.ui.label.ui

/** Budget, trip length, group size and interests that personalise TripMate AI's answers. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun PreferencesSheet(initial: TripPreferences, onDismiss: () -> Unit, onApply: (TripPreferences) -> Unit) {
    var p by remember { mutableStateOf(initial) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = TripWiseTheme.spacing.lg, vertical = TripWiseTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
        ) {
            Text(stringResource(R.string.feature_assistant_preferences), style = TripWiseTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            Text(stringResource(R.string.feature_assistant_preferences_body), style = TripWiseTheme.typography.bodyMedium, color = TripWiseTheme.colors.onSurfaceVariant)

            Label(stringResource(R.string.feature_assistant_budget))
            val budgets = listOf<BudgetLevel?>(null) + BudgetLevel.entries
            TwSegmentedButtons(
                budgets.map { b -> stringResource(b?.label ?: R.string.feature_assistant_budget_any) },
                budgets.indexOf(p.budgetLevel),
                { p = p.copy(budgetLevel = budgets[it]) },
            )

            Stepper(
                label = stringResource(R.string.feature_assistant_days),
                value = p.days?.let { pluralStringResource(R.plurals.feature_assistant_days_count, it, it) },
                onDecrease = { p = p.copy(days = p.days?.let { if (it <= 1) null else it - 1 }) },
                onIncrease = { p = p.copy(days = ((p.days ?: 0) + 1).coerceAtMost(30)) },
            )
            Stepper(
                label = stringResource(R.string.feature_assistant_travellers),
                value = p.travellers?.let { pluralStringResource(R.plurals.feature_assistant_travellers_count, it, it) },
                onDecrease = { p = p.copy(travellers = p.travellers?.let { if (it <= 1) null else it - 1 }) },
                onIncrease = { p = p.copy(travellers = ((p.travellers ?: 0) + 1).coerceAtMost(20)) },
            )

            Label(stringResource(R.string.feature_assistant_interests))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm)) {
                TravelInterest.entries.forEach { i ->
                    TwFilterChip(
                        stringResource(i.ui.label), i in p.interests,
                        { p = p.copy(interests = if (i in p.interests) p.interests - i else p.interests + i) },
                        leadingIcon = i.ui.icon,
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = TripWiseTheme.spacing.md), horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md)) {
                TwOutlinedButton(stringResource(R.string.feature_assistant_clear), { p = TripPreferences() }, Modifier.weight(1f))
                TwButton(stringResource(R.string.feature_assistant_apply), { onApply(p) }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Label(text: String) =
    Text(text, style = TripWiseTheme.typography.labelLarge, color = TripWiseTheme.colors.secondary)

@Composable
private fun Stepper(label: String, value: String?, onDecrease: () -> Unit, onIncrease: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Label(label)
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrease, enabled = value != null) {
                Icon(TwIcons.Remove, stringResource(R.string.feature_assistant_decrease, label))
            }
            Text(
                value ?: stringResource(R.string.feature_assistant_not_set),
                style = TripWiseTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 112.dp),
            )
            IconButton(onClick = onIncrease) { Icon(TwIcons.Add, stringResource(R.string.feature_assistant_increase, label)) }
        }
    }
}
