package com.practice.expensetracker.ui.common

import com.practice.expensetracker.data.Category
import com.practice.expensetracker.data.Currency
import com.practice.expensetracker.data.PaymentMethod

/**
 * Every test tag in the app. With testTagsAsResourceId enabled (see [appiumRoot]),
 * each tag shows up in Appium as the element's resource-id, e.g. By.id("add_expense_fab").
 */
object TestTags {
    // Navigation
    const val BOTTOM_NAV = "bottom_nav"
    const val NAV_EXPENSES = "nav_expenses"
    const val NAV_STATS = "nav_stats"
    const val NAV_SETTINGS = "nav_settings"
    const val SCREEN_TITLE = "screen_title"
    const val BACK_BUTTON = "back_button"
    const val SNACKBAR = "snackbar"
    const val SNACKBAR_MESSAGE = "snackbar_message"
    const val SNACKBAR_ACTION = "snackbar_action"

    // Expense list
    const val OVERFLOW_MENU_BUTTON = "overflow_menu_button"
    const val OVERFLOW_MENU = "overflow_menu"
    const val MENU_DEBUG = "menu_debug"
    const val MENU_LOAD_SAMPLE = "menu_load_sample"
    const val SPENT_CARD = "spent_card"
    const val SPENT_AMOUNT = "spent_amount"
    const val BUDGET_TEXT = "budget_text"
    const val BUDGET_PROGRESS = "budget_progress"
    const val BUDGET_PERCENT = "budget_percent"
    const val BUDGET_WARNING_BANNER = "budget_warning_banner"
    const val BUDGET_OVER_BANNER = "budget_over_banner"
    const val SEARCH_FIELD = "search_field"
    const val SEARCH_CLEAR_BUTTON = "search_clear_button"
    const val CATEGORY_FILTER_ROW = "category_filter_row"
    const val SORT_BUTTON = "sort_button"
    const val SORT_MENU = "sort_menu"
    const val EXPENSE_COUNT_TEXT = "expense_count_text"
    const val EXPENSE_LIST = "expense_list"
    const val PULL_TO_REFRESH = "pull_to_refresh"
    const val LOADING_INDICATOR = "loading_indicator"
    const val EMPTY_STATE = "empty_state"
    const val EMPTY_STATE_ADD_BUTTON = "empty_state_add_button"
    const val EMPTY_FILTER_STATE = "empty_filter_state"
    const val CLEAR_FILTERS_BUTTON = "clear_filters_button"
    const val ADD_EXPENSE_FAB = "add_expense_fab"
    const val DELETE_DIALOG = "delete_dialog"
    const val DELETE_DIALOG_CONFIRM = "delete_dialog_confirm"
    const val DELETE_DIALOG_CANCEL = "delete_dialog_cancel"

    fun categoryChip(category: Category) = "filter_chip_${category.name}"
    fun periodChip(period: String) = "period_chip_$period"
    fun sortOption(option: String) = "sort_option_$option"
    fun expenseItem(id: Long) = "expense_item_$id"
    fun expenseTitle(id: Long) = "expense_item_title_$id"
    fun expenseAmount(id: Long) = "expense_item_amount_$id"
    fun expenseCategory(id: Long) = "expense_item_category_$id"
    fun expenseDate(id: Long) = "expense_item_date_$id"
    fun expenseReimbursable(id: Long) = "expense_item_reimbursable_$id"

    // Add / edit
    const val INPUT_TITLE = "input_title"
    const val INPUT_TITLE_COUNTER = "input_title_counter"
    const val INPUT_AMOUNT = "input_amount"
    const val INPUT_CATEGORY = "input_category"
    const val INPUT_CATEGORY_VALUE = "input_category_value"
    const val CATEGORY_MENU = "category_menu"
    const val INPUT_DATE = "input_date"
    const val INPUT_DATE_VALUE = "input_date_value"
    const val DATE_PICKER_DIALOG = "date_picker_dialog"
    const val DATE_PICKER_OK = "date_picker_ok"
    const val DATE_PICKER_CANCEL = "date_picker_cancel"
    const val INPUT_NOTE = "input_note"
    const val CHECKBOX_REIMBURSABLE = "checkbox_reimbursable"
    const val ERROR_TITLE = "error_title"
    const val ERROR_AMOUNT = "error_amount"
    const val ERROR_CATEGORY = "error_category"
    const val SAVE_BUTTON = "save_button"
    const val SAVE_PROGRESS = "save_progress"
    const val DELETE_BUTTON = "delete_button"
    const val EDIT_LOADING = "edit_loading"
    const val EDIT_NOT_FOUND = "edit_not_found"
    const val DISCARD_DIALOG = "discard_dialog"
    const val DISCARD_CONFIRM = "discard_confirm"
    const val DISCARD_CANCEL = "discard_cancel"

    fun categoryOption(category: Category) = "category_option_${category.name}"
    fun paymentOption(method: PaymentMethod) = "payment_option_${method.name}"

    // Stats
    const val STATS_TOTAL = "stats_total"
    const val STATS_COUNT = "stats_count"
    const val STATS_AVG_PER_DAY = "stats_avg_per_day"
    const val STATS_EMPTY = "stats_empty"
    const val PIE_CHART = "pie_chart"
    const val BAR_CHART = "bar_chart"

    fun statsPeriod(period: String) = "stats_period_$period"
    fun legendItem(category: Category) = "legend_item_${category.name}"
    fun legendAmount(category: Category) = "legend_amount_${category.name}"
    fun legendPercent(category: Category) = "legend_percent_${category.name}"

    // Settings
    const val SETTINGS_CURRENCY = "settings_currency"
    const val SETTINGS_CURRENCY_VALUE = "settings_currency_value"
    const val CURRENCY_DIALOG = "currency_dialog"
    const val SETTINGS_BUDGET_INPUT = "settings_budget_input"
    const val SETTINGS_BUDGET_SAVE = "settings_budget_save"
    const val SETTINGS_BUDGET_ERROR = "settings_budget_error"
    const val SETTINGS_CONFIRM_DELETE_SWITCH = "settings_confirm_delete_switch"
    const val SETTINGS_OPEN_DEBUG = "settings_open_debug"

    fun currencyOption(currency: Currency) = "currency_option_${currency.name}"

    // Debug
    const val DEBUG_EXPENSE_COUNT = "debug_expense_count"
    const val DEBUG_COUNT_INPUT = "debug_count_input"
    const val DEBUG_GENERATE_BUTTON = "debug_generate_button"
    const val DEBUG_ADD_RANDOM = "debug_add_random"
    const val DEBUG_LOAD_SAMPLE = "debug_load_sample"
    const val DEBUG_SLOW_MODE_SWITCH = "debug_slow_mode_switch"
    const val DEBUG_RESET_SETTINGS = "debug_reset_settings"
    const val DEBUG_CLEAR_ALL = "debug_clear_all"
    const val DEBUG_STATUS_TEXT = "debug_status_text"
}
