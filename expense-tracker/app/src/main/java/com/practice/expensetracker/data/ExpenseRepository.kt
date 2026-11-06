package com.practice.expensetracker.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ExpenseRepository(
    private val dao: ExpenseDao,
    private val settings: SettingsStore,
) {
    val expenses: Flow<List<Expense>> = dao.observeAll()
    val count: Flow<Int> = dao.observeCount()

    suspend fun get(id: Long): Expense? {
        simulateNetwork()
        return dao.getById(id)
    }

    suspend fun add(
        title: String,
        amount: Double,
        category: Category,
        paymentMethod: PaymentMethod,
        note: String = "",
        reimbursable: Boolean = false,
        date: LocalDate = LocalDate.now(),
    ): Long {
        simulateNetwork()
        return dao.insert(
            Expense(
                title = title,
                amount = amount,
                category = category,
                date = date,
                paymentMethod = paymentMethod,
                note = note,
                reimbursable = reimbursable,
            )
        )
    }

    suspend fun update(expense: Expense) {
        simulateNetwork()
        dao.update(expense)
    }

    suspend fun delete(expense: Expense) {
        dao.delete(expense)
    }

    suspend fun insertAll(expenses: List<Expense>) = dao.insertAll(expenses)

    suspend fun deleteAll() = dao.deleteAll()

    /** Debug "slow mode" pretends every write goes over a slow network. */
    private suspend fun simulateNetwork() {
        if (settings.slowMode.value) delay(SLOW_WRITE_MS)
    }

    companion object {
        const val SLOW_WRITE_MS = 1500L
        const val SLOW_LOAD_MS = 2500L
    }
}
