package com.practice.expensetracker.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _currency = MutableStateFlow(
        runCatching { Currency.valueOf(prefs.getString(KEY_CURRENCY, null) ?: "") }
            .getOrDefault(Currency.INR)
    )
    val currency: StateFlow<Currency> = _currency.asStateFlow()

    private val _budget = MutableStateFlow(prefs.getFloat(KEY_BUDGET, DEFAULT_BUDGET).toDouble())
    val budget: StateFlow<Double> = _budget.asStateFlow()

    private val _confirmDelete = MutableStateFlow(prefs.getBoolean(KEY_CONFIRM_DELETE, true))
    val confirmDelete: StateFlow<Boolean> = _confirmDelete.asStateFlow()

    private val _slowMode = MutableStateFlow(prefs.getBoolean(KEY_SLOW_MODE, false))
    val slowMode: StateFlow<Boolean> = _slowMode.asStateFlow()

    fun setCurrency(value: Currency) {
        prefs.edit { putString(KEY_CURRENCY, value.name) }
        _currency.value = value
    }

    fun setBudget(value: Double) {
        prefs.edit { putFloat(KEY_BUDGET, value.toFloat()) }
        _budget.value = value
    }

    fun setConfirmDelete(value: Boolean) {
        prefs.edit { putBoolean(KEY_CONFIRM_DELETE, value) }
        _confirmDelete.value = value
    }

    fun setSlowMode(value: Boolean) {
        prefs.edit { putBoolean(KEY_SLOW_MODE, value) }
        _slowMode.value = value
    }

    fun reset() {
        setCurrency(Currency.INR)
        setBudget(DEFAULT_BUDGET.toDouble())
        setConfirmDelete(true)
        setSlowMode(false)
    }

    companion object {
        const val DEFAULT_BUDGET = 20000f
        private const val KEY_CURRENCY = "currency"
        private const val KEY_BUDGET = "budget"
        private const val KEY_CONFIRM_DELETE = "confirm_delete"
        private const val KEY_SLOW_MODE = "slow_mode"
    }
}
