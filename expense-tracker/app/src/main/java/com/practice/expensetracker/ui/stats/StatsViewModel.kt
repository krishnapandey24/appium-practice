package com.practice.expensetracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practice.expensetracker.data.Category
import com.practice.expensetracker.data.Currency
import com.practice.expensetracker.data.Expense
import com.practice.expensetracker.data.ExpenseRepository
import com.practice.expensetracker.data.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

enum class StatsPeriod(val label: String) {
    THIS_MONTH("This month"),
    LAST_6_MONTHS("6 months"),
    THIS_YEAR("This year"),
}

data class CategorySlice(val category: Category, val amount: Double, val percent: Int)
data class BarEntry(val label: String, val amount: Double)

data class StatsUiState(
    val period: StatsPeriod = StatsPeriod.THIS_MONTH,
    val currency: Currency = Currency.INR,
    val total: Double = 0.0,
    val count: Int = 0,
    val avgPerDay: Double = 0.0,
    val slices: List<CategorySlice> = emptyList(),
    val bars: List<BarEntry> = emptyList(),
    val highest: Expense? = null,
)

class StatsViewModel(
    repository: ExpenseRepository,
    settings: SettingsStore,
) : ViewModel() {

    private val currency = settings.currency.value
    private val period = MutableStateFlow(StatsPeriod.THIS_MONTH)

    val state: StateFlow<StatsUiState> = combine(repository.expenses, period) { expenses, p ->
        build(expenses, p, LocalDate.now())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState(currency = currency))

    fun onPeriodChange(value: StatsPeriod) {
        period.value = value
    }

    private fun build(all: List<Expense>, p: StatsPeriod, today: LocalDate): StatsUiState {
        val (start, end) = when (p) {
            StatsPeriod.THIS_MONTH -> today.withDayOfMonth(1) to today.withDayOfMonth(today.lengthOfMonth())
            StatsPeriod.LAST_6_MONTHS -> today.minusMonths(5).withDayOfMonth(1) to today.withDayOfMonth(today.lengthOfMonth())
            StatsPeriod.THIS_YEAR -> today.withDayOfYear(1) to today.withDayOfYear(today.lengthOfYear())
        }
        val inPeriod = all.filter { !it.date.isBefore(start) && !it.date.isAfter(end) }
        val total = inPeriod.sumOf { it.amount }
        val daysElapsed = (ChronoUnit.DAYS.between(start, today) + 1).coerceAtLeast(1)

        val slices = inPeriod.groupBy { it.category }
            .map { (category, items) ->
                val amount = items.sumOf { it.amount }
                CategorySlice(category, amount, (amount / total * 100).toInt())
            }
            .sortedByDescending { it.amount }

        val bars = when (p) {
            StatsPeriod.THIS_MONTH -> (1..today.lengthOfMonth()).map { day ->
                BarEntry(day.toString(), inPeriod.filter { it.date.dayOfMonth == day }.sumOf { it.amount })
            }
            else -> {
                var month = start
                buildList {
                    while (!month.isAfter(end)) {
                        val m = month
                        add(
                            BarEntry(
                                m.month.getDisplayName(TextStyle.SHORT, Locale.US),
                                inPeriod.filter { it.date.year == m.year && it.date.month == m.month }.sumOf { it.amount },
                            )
                        )
                        month = month.plusMonths(1)
                    }
                }
            }
        }

        return StatsUiState(
            period = p,
            currency = currency,
            total = total,
            count = inPeriod.size,
            avgPerDay = total / daysElapsed,
            slices = slices,
            bars = bars,
            highest = inPeriod.maxByOrNull { it.amount },
        )
    }
}
