package com.tripwise.core.data.repository

import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.common.result.outcomeOf
import com.tripwise.core.data.mapper.enumOr
import com.tripwise.core.database.dao.BudgetDao
import com.tripwise.core.database.dao.ExpenseDao
import com.tripwise.core.database.dao.TripDao
import com.tripwise.core.database.entity.BudgetAllocationEntity
import com.tripwise.core.database.entity.ExpenseEntity
import com.tripwise.core.domain.repository.CurrencyRepository
import com.tripwise.core.domain.repository.ExpenseRepository
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.ExchangeRates
import com.tripwise.core.model.Expense
import com.tripwise.core.model.ExpenseCategory
import com.tripwise.core.model.ExpenseDraft
import com.tripwise.core.model.Money
import com.tripwise.core.network.api.CurrencyApi
import com.tripwise.core.network.error.ApiErrorMapper
import com.tripwise.core.network.error.call
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

internal class OfflineFirstExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val tripDao: TripDao,
    private val clock: Clock,
) : ExpenseRepository {

    override fun observeExpenses(tripId: String): Flow<List<Expense>> = expenseDao.observeForTrip(tripId).map { list ->
        list.map { Expense(it.id, it.tripId, enumOr(it.category, ExpenseCategory.OTHER), Money(it.amountMinor, CurrencyCode(it.currency)), it.note, it.spentOn) }
    }

    override suspend fun saveExpense(tripId: String, expenseId: String?, draft: ExpenseDraft): Outcome<Unit> =
        outcomeOf({ AppError.Unknown(it) }) {
            require(draft.amount.amountMinor > 0) { "amount must be positive" }
            expenseDao.upsert(
                ExpenseEntity(
                    id = expenseId ?: UUID.randomUUID().toString(), tripId = tripId, category = draft.category.name,
                    amountMinor = draft.amount.amountMinor, currency = draft.amount.currency.code,
                    note = draft.note?.trim()?.take(120)?.ifEmpty { null }, spentOn = draft.spentOn, updatedAt = Instant.now(clock),
                ),
            )
        }

    override suspend fun deleteExpense(expenseId: String) = expenseDao.markDeleted(expenseId, Instant.now(clock))

    override suspend fun setPlan(tripId: String, planned: Map<ExpenseCategory, Money>) {
        val now = Instant.now(clock)
        budgetDao.clearAllocations(tripId)
        budgetDao.upsertAllocations(planned.map { (c, m) -> BudgetAllocationEntity(tripId, c.name, m.amountMinor, m.currency.code, now) })
        val currency = planned.values.firstOrNull()?.currency?.code
        tripDao.updateBudget(tripId, planned.values.sumOf { it.amountMinor }.takeIf { currency != null }, currency, now)
    }

    override suspend fun setPlanned(tripId: String, category: ExpenseCategory, amount: Money) {
        val now = Instant.now(clock)
        budgetDao.upsertAllocations(listOf(BudgetAllocationEntity(tripId, category.name, amount.amountMinor, amount.currency.code, now)))
        val all = budgetDao.observeAllocations(tripId).first().filter { it.currency == amount.currency.code }
        tripDao.updateBudget(tripId, all.sumOf { it.plannedMinor }, amount.currency.code, now)
    }
}

/** Exchange rates from the backend, cached in memory for the app session. */
@Singleton
internal class DefaultCurrencyRepository @Inject constructor(
    private val api: CurrencyApi,
    private val errors: ApiErrorMapper,
) : CurrencyRepository {
    private val mutex = Mutex()
    @Volatile private var cached: ExchangeRates? = null

    override suspend fun usdRates(): Outcome<ExchangeRates> = mutex.withLock {
        cached?.let { return@withLock Outcome.Success(it) }
        when (val r = errors.call { api.rates() }) {
            is Outcome.Success -> ExchangeRates(r.value.rates, live = r.value.source == "live")
                .also { if (it.live) cached = it }.let { Outcome.Success(it) }
            is Outcome.Failure -> r
        }
    }
}
