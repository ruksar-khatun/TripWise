package com.tripwise.feature.trips

import androidx.lifecycle.SavedStateHandle
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.domain.usecase.EstimateTripCostUseCase
import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.ExpenseCategory
import com.tripwise.core.model.Money
import com.tripwise.core.testing.data.TestData
import com.tripwise.core.testing.fakes.FakeCurrencyRepository
import com.tripwise.core.testing.fakes.FakeDestinationRepository
import com.tripwise.core.testing.fakes.FakeExpenseRepository
import com.tripwise.core.testing.fakes.FakeTripRepository
import com.tripwise.core.testing.fakes.FakeUserRepository
import com.tripwise.core.testing.rules.MainDispatcherRule
import com.tripwise.core.testing.rules.collectInBackground
import com.tripwise.feature.trips.budget.BudgetAction
import com.tripwise.feature.trips.budget.BudgetViewModel
import com.tripwise.feature.trips.detail.TripDetailAction
import com.tripwise.feature.trips.detail.TripDetailViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

class BudgetAndItineraryTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val clock = Clock.fixed(Instant.parse("2026-03-01T10:00:00Z"), ZoneOffset.UTC)
    private val usd = CurrencyCode("USD")
    private val inr = CurrencyCode("INR")
    private val kyoto = TestData.destination("kyoto").copy(dailyCostUsd = mapOf(ExpenseCategory.FOOD to Money(5_000, usd)))
    private val trip = TestData.trip("t1", LocalDate.of(2026, 4, 1), days = 2, destinationId = "kyoto").copy(budget = null)

    @Test fun `amount parsing respects the currency's minor units`() {
        assertEquals(Money(123_450, inr), BudgetViewModel.parseAmount("1,234.50", inr))
        assertEquals(Money(1_250, inr), BudgetViewModel.parseAmount("12,5", inr)) // decimal comma
        assertEquals(Money(1_500, CurrencyCode("JPY")), BudgetViewModel.parseAmount("1500", CurrencyCode("JPY")))
        assertNull(BudgetViewModel.parseAmount("1.5", CurrencyCode("JPY"))) // JPY has no minor unit
        assertNull(BudgetViewModel.parseAmount("-3", inr))
        assertNull(BudgetViewModel.parseAmount("0", inr))
        assertEquals(Money(0, inr), BudgetViewModel.parseAmount("0", inr, allowZero = true))
        assertEquals("1234.5", BudgetViewModel.toMajorText(Money(123_450, inr)))
    }

    @Test fun `estimate in home currency, apply as plan, and record an expense`() = runTest {
        val trips = FakeTripRepository(listOf(trip))
        val expenses = FakeExpenseRepository()
        val vm = BudgetViewModel(
            SavedStateHandle(mapOf("tripId" to "t1")), trips, expenses, FakeDestinationRepository(listOf(kyoto)),
            FakeUserRepository(TestData.profile), EstimateTripCostUseCase(FakeCurrencyRepository()), clock,
        )
        collectInBackground(vm.uiState)
        assertEquals(inr, vm.uiState.value.currency) // no trip budget → user's home currency

        vm.onAction(BudgetAction.Estimate)
        val estimate = (vm.uiState.value.estimate as LoadState.Ready).data
        // $50 × 2 days × 2 travellers × 80 = ₹16,000
        assertEquals(Money(1_600_000, inr), estimate.perCategory[ExpenseCategory.FOOD])
        vm.onAction(BudgetAction.ApplyEstimate)
        assertEquals(estimate.perCategory, expenses.plans["t1"])

        vm.onAction(BudgetAction.AddExpense)
        vm.onAction(BudgetAction.UpdateExpenseEditor(vm.uiState.value.expenseEditor!!.copy(amount = "abc")))
        vm.onAction(BudgetAction.SaveExpense)
        assertEquals(true, vm.uiState.value.expenseEditor?.showErrors) // invalid amount keeps dialog open
        vm.onAction(BudgetAction.UpdateExpenseEditor(vm.uiState.value.expenseEditor!!.copy(amount = "450", note = "Ramen")))
        vm.onAction(BudgetAction.SaveExpense)
        assertNull(vm.uiState.value.expenseEditor)
        assertEquals(listOf("Ramen" to Money(45_000, inr)), vm.uiState.value.expenses.map { it.note to it.amount })
        assertEquals(LocalDate.of(2026, 3, 1), vm.uiState.value.expenses.single().spentOn)
    }

    @Test fun `add, edit and delete itinerary activities`() = runTest {
        val trips = FakeTripRepository(listOf(trip))
        val vm = TripDetailViewModel(SavedStateHandle(mapOf("tripId" to "t1")), trips, clock)
        collectInBackground(vm.uiState)
        vm.onAction(TripDetailAction.AddItem(trip.startDate))
        vm.onAction(TripDetailAction.UpdateEditor(vm.editor.value!!.copy(title = "Tea ceremony", time = "25:00")))
        vm.onAction(TripDetailAction.SaveItem)
        assertEquals(true, vm.editor.value?.showErrors) // invalid time
        vm.onAction(TripDetailAction.UpdateEditor(vm.editor.value!!.copy(time = "9:30", category = ActivityCategory.ACTIVITY)))
        vm.onAction(TripDetailAction.SaveItem)
        val item = trips.items.value.single()
        assertEquals(LocalTime.of(9, 30), item.startTime)
        assertEquals("Tea ceremony", item.title)

        vm.onAction(TripDetailAction.EditItem(item))
        vm.onAction(TripDetailAction.UpdateEditor(vm.editor.value!!.copy(title = "Matcha workshop")))
        vm.onAction(TripDetailAction.SaveItem)
        assertEquals(listOf("Matcha workshop"), trips.items.value.map { it.title })

        vm.onAction(TripDetailAction.EditItem(trips.items.value.single()))
        vm.onAction(TripDetailAction.DeleteItem)
        assertEquals(emptyList<Any>(), trips.items.value)
    }
}
