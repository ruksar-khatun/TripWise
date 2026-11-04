package com.tripwise.core.domain.repository

import com.tripwise.core.common.result.Outcome
import com.tripwise.core.model.ExchangeRates
import com.tripwise.core.model.Expense
import com.tripwise.core.model.ExpenseCategory
import com.tripwise.core.model.ExpenseDraft
import com.tripwise.core.model.Money
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    /** Newest first. */
    fun observeExpenses(tripId: String): Flow<List<Expense>>

    /** Creates when [expenseId] is null, otherwise updates. */
    suspend fun saveExpense(tripId: String, expenseId: String?, draft: ExpenseDraft): Outcome<Unit>

    suspend fun deleteExpense(expenseId: String)

    /** Replaces the planned amount per category and sets the trip's total budget to their sum. */
    suspend fun setPlan(tripId: String, planned: Map<ExpenseCategory, Money>)

    /** Updates one category's planned amount (total budget is recalculated). */
    suspend fun setPlanned(tripId: String, category: ExpenseCategory, amount: Money)
}

interface CurrencyRepository {
    suspend fun usdRates(): Outcome<ExchangeRates>
}
