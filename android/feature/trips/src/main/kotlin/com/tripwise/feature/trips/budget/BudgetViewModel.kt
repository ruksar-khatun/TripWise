package com.tripwise.feature.trips.budget

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.domain.repository.ExpenseRepository
import com.tripwise.core.domain.repository.TripRepository
import com.tripwise.core.domain.repository.UserRepository
import com.tripwise.core.domain.usecase.EstimateTripCostUseCase
import com.tripwise.core.model.BudgetLevel
import com.tripwise.core.model.BudgetSummary
import com.tripwise.core.model.CostEstimate
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.Expense
import com.tripwise.core.model.ExpenseCategory
import com.tripwise.core.model.ExpenseDraft
import com.tripwise.core.model.Money
import com.tripwise.core.model.Trip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** Add/edit expense dialog. [id] null = new expense. */
data class ExpenseEditor(
    val id: String?,
    val category: ExpenseCategory,
    val amount: String,
    val note: String,
    val date: LocalDate,
    val showErrors: Boolean = false,
)

data class PlannedEditor(val category: ExpenseCategory, val amount: String, val showErrors: Boolean = false)

data class BudgetUiState(
    val trip: Trip? = null,
    val budget: BudgetSummary? = null,
    val expenses: List<Expense> = emptyList(),
    val currency: CurrencyCode = CurrencyCode("INR"),
    val level: BudgetLevel = BudgetLevel.MID,
    val estimate: LoadState<CostEstimate>? = null,
    val expenseEditor: ExpenseEditor? = null,
    val plannedEditor: PlannedEditor? = null,
    val error: AppError? = null,
)

sealed interface BudgetAction {
    data class SetLevel(val level: BudgetLevel) : BudgetAction
    data object Estimate : BudgetAction
    data object ApplyEstimate : BudgetAction
    data object AddExpense : BudgetAction
    data class EditExpense(val expense: Expense) : BudgetAction
    data class UpdateExpenseEditor(val editor: ExpenseEditor) : BudgetAction
    data object SaveExpense : BudgetAction
    data object DeleteExpense : BudgetAction
    data class EditPlanned(val category: ExpenseCategory) : BudgetAction
    data class UpdatePlannedEditor(val amount: String) : BudgetAction
    data object SavePlanned : BudgetAction
    data object DismissDialog : BudgetAction
}

@HiltViewModel
class BudgetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    trips: TripRepository,
    private val expenses: ExpenseRepository,
    private val destinations: DestinationRepository,
    users: UserRepository,
    private val estimateCost: EstimateTripCostUseCase,
    private val clock: Clock,
) : ViewModel() {
    private val tripId: String = checkNotNull(savedStateHandle["tripId"])
    private val tripRepository = trips
    private val local = MutableStateFlow(BudgetUiState())

    val uiState: StateFlow<BudgetUiState> = combine(
        trips.observeTrip(tripId),
        trips.observeBudget(tripId),
        expenses.observeExpenses(tripId),
        users.observeProfile().map { it.homeCurrency },
        local,
    ) { trip, budget, list, home, ui ->
        ui.copy(trip = trip, budget = budget, expenses = list, currency = trip?.budget?.currency ?: home)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetUiState())

    fun onAction(action: BudgetAction) {
        when (action) {
            is BudgetAction.SetLevel -> { local.update { it.copy(level = action.level) }; if (local.value.estimate != null) estimate() }
            BudgetAction.Estimate -> estimate()
            BudgetAction.ApplyEstimate -> applyEstimate()
            BudgetAction.AddExpense -> local.update {
                it.copy(expenseEditor = ExpenseEditor(null, ExpenseCategory.FOOD, "", "", LocalDate.now(clock)))
            }
            is BudgetAction.EditExpense -> local.update {
                val e = action.expense
                it.copy(expenseEditor = ExpenseEditor(e.id, e.category, toMajorText(e.amount), e.note.orEmpty(), e.spentOn))
            }
            is BudgetAction.UpdateExpenseEditor -> local.update { it.copy(expenseEditor = action.editor) }
            BudgetAction.SaveExpense -> saveExpense()
            BudgetAction.DeleteExpense -> {
                val id = local.value.expenseEditor?.id ?: return
                viewModelScope.launch { expenses.deleteExpense(id) }
                local.update { it.copy(expenseEditor = null) }
            }
            is BudgetAction.EditPlanned -> local.update { s ->
                val current = uiState.value.budget?.categories?.firstOrNull { it.category == action.category }?.planned
                s.copy(plannedEditor = PlannedEditor(action.category, current?.let(::toMajorText).orEmpty()))
            }
            is BudgetAction.UpdatePlannedEditor -> local.update { s -> s.copy(plannedEditor = s.plannedEditor?.copy(amount = action.amount)) }
            BudgetAction.SavePlanned -> savePlanned()
            BudgetAction.DismissDialog -> local.update { it.copy(expenseEditor = null, plannedEditor = null) }
        }
    }

    private fun estimate() {
        local.update { it.copy(estimate = LoadState.Loading) }
        viewModelScope.launch {
            val s = uiState.value
            val trip = s.trip ?: tripRepository.observeTrip(tripId).firstOrNull()
            val destination = trip?.destinationId?.let { runCatching { destinations.observeDestination(it).first() }.getOrNull() }
            val result = if (trip == null || destination == null) {
                Outcome.Failure(AppError.NotFound)
            } else {
                estimateCost(destination, trip.dayCount, trip.travellers, local.value.level, tripRepository.observeItinerary(tripId).first(), s.currency)
            }
            local.update {
                it.copy(estimate = when (result) {
                    is Outcome.Success -> LoadState.Ready(result.value)
                    is Outcome.Failure -> LoadState.Failed(result.error)
                })
            }
        }
    }

    private fun applyEstimate() {
        val estimate = (local.value.estimate as? LoadState.Ready)?.data ?: return
        viewModelScope.launch { expenses.setPlan(tripId, estimate.perCategory) }
    }

    private fun saveExpense() {
        val editor = local.value.expenseEditor ?: return
        val amount = parseAmount(editor.amount, uiState.value.currency)
        if (amount == null) {
            local.update { it.copy(expenseEditor = editor.copy(showErrors = true)) }
            return
        }
        viewModelScope.launch {
            when (val r = expenses.saveExpense(tripId, editor.id, ExpenseDraft(editor.category, amount, editor.note, editor.date))) {
                is Outcome.Success -> local.update { it.copy(expenseEditor = null, error = null) }
                is Outcome.Failure -> local.update { it.copy(error = r.error) }
            }
        }
    }

    private fun savePlanned() {
        val editor = local.value.plannedEditor ?: return
        val amount = parseAmount(editor.amount, uiState.value.currency, allowZero = true)
        if (amount == null) {
            local.update { it.copy(plannedEditor = editor.copy(showErrors = true)) }
            return
        }
        viewModelScope.launch { expenses.setPlanned(tripId, editor.category, amount) }
        local.update { it.copy(plannedEditor = null) }
    }

    internal companion object {
        private fun digits(code: CurrencyCode) =
            runCatching { java.util.Currency.getInstance(code.code).defaultFractionDigits }.getOrDefault(2).coerceAtLeast(0)

        /** "1,234.5" / "1234,50" → minor units; null if invalid, negative, or too precise for the currency. */
        fun parseAmount(text: String, currency: CurrencyCode, allowZero: Boolean = false): Money? {
            val cleaned = text.trim().replace(" ", "").let { t ->
                if (t.count { it == ',' } == 1 && !t.contains('.')) t.replace(',', '.') else t.replace(",", "")
            }
            val value = cleaned.toBigDecimalOrNull() ?: return null
            if (value.signum() < 0 || (!allowZero && value.signum() == 0) || value.scale() > digits(currency)) return null
            return runCatching { Money(value.movePointRight(digits(currency)).longValueExact(), currency) }.getOrNull()
        }

        fun toMajorText(m: Money): String = BigDecimal.valueOf(m.amountMinor, digits(m.currency)).stripTrailingZeros().toPlainString()
    }
}
