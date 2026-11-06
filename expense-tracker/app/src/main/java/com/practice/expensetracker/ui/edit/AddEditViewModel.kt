package com.practice.expensetracker.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practice.expensetracker.data.Category
import com.practice.expensetracker.data.Currency
import com.practice.expensetracker.data.Expense
import com.practice.expensetracker.data.ExpenseRepository
import com.practice.expensetracker.data.PaymentMethod
import com.practice.expensetracker.data.SettingsStore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ExpenseForm(
    val title: String = "",
    val amount: String = "",
    val category: Category? = null,
    val date: LocalDate = LocalDate.now(),
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val note: String = "",
    val reimbursable: Boolean = false,
)

data class EditUiState(
    val isEdit: Boolean = false,
    val isLoading: Boolean = false,
    val notFound: Boolean = false,
    val form: ExpenseForm = ExpenseForm(),
    val titleError: String? = null,
    val amountError: String? = null,
    val categoryError: String? = null,
    val isSaving: Boolean = false,
)

class AddEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ExpenseRepository,
    settings: SettingsStore,
    private val messages: Channel<String>,
) : ViewModel() {

    private val expenseId: Long = savedStateHandle.get<Long>(ARG_EXPENSE_ID) ?: NEW_EXPENSE
    private var original: Expense? = null
    private var initialForm = ExpenseForm()

    val currency: Currency = settings.currency.value

    private val _state = MutableStateFlow(EditUiState(isEdit = expenseId != NEW_EXPENSE, isLoading = expenseId != NEW_EXPENSE))
    val state: StateFlow<EditUiState> = _state.asStateFlow()

    private val _finished = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val finished: SharedFlow<Unit> = _finished

    init {
        if (expenseId != NEW_EXPENSE) {
            viewModelScope.launch {
                val expense = repository.get(expenseId)
                if (expense == null) {
                    _state.update { it.copy(isLoading = false, notFound = true) }
                } else {
                    original = expense
                    initialForm = expense.toForm()
                    _state.update { it.copy(isLoading = false, form = initialForm) }
                }
            }
        }
    }

    val isDirty: Boolean
        get() {
            val f = _state.value.form
            val i = initialForm
            return f.title != i.title || f.category != i.category || f.date != i.date ||
                f.paymentMethod != i.paymentMethod || f.note != i.note || f.reimbursable != i.reimbursable
        }

    fun onTitleChange(value: String) = updateForm { copy(title = value) }.also {
        _state.update { it.copy(titleError = null) }
    }

    fun onAmountChange(value: String) {
        val cleaned = value.filter { it.isDigit() || it == '.' }
        updateForm { copy(amount = cleaned) }
        _state.update { it.copy(amountError = null) }
    }

    fun onCategoryChange(value: Category) {
        updateForm { copy(category = value) }
        _state.update { it.copy(categoryError = null) }
    }

    fun onDateChange(value: LocalDate) = updateForm { copy(date = value) }
    fun onPaymentMethodChange(value: PaymentMethod) = updateForm { copy(paymentMethod = value) }
    fun onNoteChange(value: String) = updateForm { copy(note = value) }
    fun onReimbursableChange(value: Boolean) = updateForm { copy(reimbursable = value) }

    private fun updateForm(block: ExpenseForm.() -> ExpenseForm) =
        _state.update { it.copy(form = it.form.block()) }

    fun save() {
        val form = _state.value.form
        val titleError = when {
            form.title.isEmpty() -> "Title is required"
            form.title.length > TITLE_MAX -> "Title must be $TITLE_MAX characters or less"
            else -> null
        }
        val amount = form.amount.toDoubleOrNull()
        val amountError = when {
            form.amount.isBlank() -> "Amount is required"
            amount == null -> "Enter a valid amount"
            amount < 0 -> "Amount must be greater than 0"
            amount > AMOUNT_MAX -> "Amount can't be more than 10,00,000"
            else -> null
        }
        val categoryError = if (form.category == null) "Choose a category" else null

        _state.update { it.copy(titleError = titleError, amountError = amountError, categoryError = categoryError) }
        if (titleError != null || amountError != null || categoryError != null) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val existing = original
            if (existing == null) {
                repository.add(
                    title = form.title.trim(),
                    amount = amount!!,
                    category = form.category!!,
                    paymentMethod = form.paymentMethod,
                    note = form.note.trim(),
                    reimbursable = form.reimbursable,
                    date = form.date,
                )
                messages.send("Expense added")
            } else {
                repository.update(
                    existing.copy(
                        title = form.title.trim(),
                        amount = amount!!,
                        category = form.category!!,
                        date = form.date,
                        paymentMethod = form.paymentMethod,
                        note = form.note.trim(),
                        reimbursable = form.reimbursable,
                    )
                )
                messages.send("Expense updated")
            }
            _state.update { it.copy(isSaving = false) }
            _finished.emit(Unit)
        }
    }

    fun delete() {
        val existing = original ?: return
        viewModelScope.launch {
            repository.delete(existing)
            messages.send("Expense deleted")
            _finished.emit(Unit)
        }
    }

    private fun Expense.toForm() = ExpenseForm(
        title = title,
        amount = if (amount % 1.0 == 0.0) amount.toLong().toString() else amount.toString(),
        category = category,
        date = date,
        paymentMethod = paymentMethod,
        note = note,
        reimbursable = reimbursable,
    )

    companion object {
        const val ARG_EXPENSE_ID = "expenseId"
        const val NEW_EXPENSE = -1L
        const val TITLE_MAX = 40
        const val AMOUNT_MAX = 1_000_000.0
    }
}
