package com.practice.expensetracker.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practice.expensetracker.data.Category
import com.practice.expensetracker.data.Currency
import com.practice.expensetracker.data.Expense
import com.practice.expensetracker.data.ExpenseRepository
import com.practice.expensetracker.data.SampleData
import com.practice.expensetracker.data.SettingsStore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class PeriodFilter(val label: String) {
    ALL("All time"),
    THIS_MONTH("This month"),
    LAST_30_DAYS("Last 30 days"),
}

enum class SortOption(val label: String) {
    DATE_DESC("Newest first"),
    DATE_ASC("Oldest first"),
    AMOUNT_DESC("Amount: high to low"),
    AMOUNT_ASC("Amount: low to high"),
}

data class Filters(
    val query: String = "",
    val categories: List<Category> = emptyList(),
    val period: PeriodFilter = PeriodFilter.ALL,
    val sort: SortOption = SortOption.DATE_DESC,
) {
    val isActive: Boolean
        get() = query.isNotBlank() || categories.isNotEmpty() || period != PeriodFilter.ALL
}

data class ListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val expenses: List<Expense> = emptyList(),
    val totalInDb: Int = 0,
    val monthSpent: Double = 0.0,
    val budget: Double = 0.0,
    val currency: Currency = Currency.INR,
    val filters: Filters = Filters(),
)

class ExpenseListViewModel(
    private val repository: ExpenseRepository,
    private val settings: SettingsStore,
    private val messages: Channel<String>,
) : ViewModel() {

    private val filters = MutableStateFlow(Filters())
    private val loading = MutableStateFlow(LoadState(initial = true, refreshing = false))
    private val all = MutableStateFlow<List<Expense>?>(null)

    private val _pendingDelete = MutableStateFlow<Expense?>(null)
    val pendingDelete: StateFlow<Expense?> = _pendingDelete.asStateFlow()

    private val _deleted = MutableSharedFlow<Expense>(extraBufferCapacity = 1)
    val deleted: SharedFlow<Expense> = _deleted

    val state: StateFlow<ListUiState> = combine(
        all, filters, loading, settings.currency, settings.budget,
    ) { expenses, f, load, currency, budget ->
        val list = expenses.orEmpty()
        ListUiState(
            isLoading = expenses == null || load.initial,
            isRefreshing = load.refreshing,
            expenses = applyFilters(list, f, LocalDate.now()),
            totalInDb = list.size,
            monthSpent = monthTotal(list, LocalDate.now()),
            budget = budget,
            currency = currency,
            filters = f,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListUiState())

    init {
        viewModelScope.launch { repository.expenses.collect { all.value = it } }
        viewModelScope.launch {
            if (settings.slowMode.value) delay(ExpenseRepository.SLOW_LOAD_MS)
            loading.value = LoadState(initial = false, refreshing = false)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            loading.value = loading.value.copy(refreshing = true)
            delay(if (settings.slowMode.value) ExpenseRepository.SLOW_LOAD_MS else 800)
            loading.value = loading.value.copy(refreshing = false)
        }
    }

    fun onQueryChange(query: String) = filters.update { it.copy(query = query) }

    fun toggleCategory(category: Category) = filters.update {
        val selected = if (category in it.categories) it.categories - category else it.categories + category
        it.copy(categories = selected)
    }

    fun onPeriodChange(period: PeriodFilter) = filters.update { it.copy(period = period) }

    fun onSortChange(sort: SortOption) = filters.update { it.copy(sort = sort) }

    fun clearFilters() = filters.update { Filters(sort = it.sort) }

    fun requestDelete(expense: Expense) {
        if (settings.confirmDelete.value) _pendingDelete.value = expense else delete(expense)
    }

    fun confirmDelete() {
        _pendingDelete.value?.let { delete(it) }
        _pendingDelete.value = null
    }

    fun cancelDelete() {
        _pendingDelete.value = null
    }

    private fun delete(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense)
            _deleted.emit(expense)
        }
    }

    fun undoDelete(expense: Expense) {
        viewModelScope.launch {
            repository.add(
                title = expense.title,
                amount = expense.amount,
                category = expense.category,
                paymentMethod = expense.paymentMethod,
                note = expense.note,
                reimbursable = expense.reimbursable,
            )
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            repository.deleteAll()
            repository.insertAll(SampleData.fixed())
            messages.send("Sample data loaded")
        }
    }

    private data class LoadState(val initial: Boolean, val refreshing: Boolean)

    companion object {
        fun applyFilters(list: List<Expense>, f: Filters, today: LocalDate): List<Expense> {
            var result = list
            val query = f.query.trim()
            if (query.isNotEmpty()) {
                result = result.filter { it.title.contains(query) || it.note.contains(query) }
            }
            if (f.categories.isNotEmpty()) {
                val category = f.categories.last()
                result = result.filter { it.category == category }
            }
            result = when (f.period) {
                PeriodFilter.ALL -> result
                PeriodFilter.THIS_MONTH -> {
                    val (start, end) = monthRange(today)
                    result.filter { it.date.isInRange(start, end) }
                }
                PeriodFilter.LAST_30_DAYS -> {
                    val start = today.minusDays(29)
                    result.filter { !it.date.isBefore(start) && !it.date.isAfter(today) }
                }
            }
            return when (f.sort) {
                SortOption.DATE_DESC -> result.sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.id })
                SortOption.DATE_ASC -> result.sortedWith(compareBy<Expense> { it.date }.thenBy { it.id })
                SortOption.AMOUNT_DESC -> result.sortedByDescending { amountKey(it) }
                SortOption.AMOUNT_ASC -> result.sortedBy { amountKey(it) }
            }
        }

        private fun amountKey(expense: Expense) = expense.amount.toString()

        fun monthRange(today: LocalDate): Pair<LocalDate, LocalDate> =
            today.withDayOfMonth(1) to today.withDayOfMonth(today.lengthOfMonth())

        private fun LocalDate.isInRange(start: LocalDate, end: LocalDate) =
            !isBefore(start) && isBefore(end)

        fun monthTotal(list: List<Expense>, today: LocalDate): Double {
            val (start, end) = monthRange(today)
            return list.filter { it.date.isInRange(start, end) }.sumOf { it.amount }
        }
    }
}
