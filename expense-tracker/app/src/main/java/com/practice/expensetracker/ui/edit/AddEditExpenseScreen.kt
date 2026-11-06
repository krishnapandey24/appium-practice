package com.practice.expensetracker.ui.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.practice.expensetracker.data.Category
import com.practice.expensetracker.data.PaymentMethod
import com.practice.expensetracker.ui.common.AppViewModelFactory
import com.practice.expensetracker.ui.common.DeleteExpenseDialog
import com.practice.expensetracker.ui.common.TestTags
import com.practice.expensetracker.ui.common.appiumRoot
import com.practice.expensetracker.ui.common.color
import com.practice.expensetracker.ui.common.formatDate
import com.practice.expensetracker.ui.common.icon
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    onDone: () -> Unit,
    viewModel: AddEditViewModel = viewModel(factory = AppViewModelFactory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.finished.collect { onDone() } }

    val onBack = { if (viewModel.isDirty) showDiscardDialog = true else onDone() }
    BackHandler(onBack = onBack)

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEdit) "Edit expense" else "Add expense",
                        modifier = Modifier.testTag(TestTags.SCREEN_TITLE),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag(TestTags.BACK_BUTTON)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                    }
                },
                actions = {
                    if (state.isEdit && !state.notFound && !state.isLoading) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag(TestTags.DELETE_BUTTON),
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete expense")
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.testTag(TestTags.EDIT_LOADING))
            }
            state.notFound -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "This expense doesn't exist anymore.",
                    modifier = Modifier.testTag(TestTags.EDIT_NOT_FOUND),
                )
            }
            else -> ExpenseFormContent(
                state = state,
                viewModel = viewModel,
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            modifier = Modifier.appiumRoot().testTag(TestTags.DISCARD_DIALOG),
            title = { Text("Discard changes?") },
            text = { Text("Your changes to this expense will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = { showDiscardDialog = false; onDone() },
                    modifier = Modifier.testTag(TestTags.DISCARD_CONFIRM),
                ) { Text("Discard") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscardDialog = false },
                    modifier = Modifier.testTag(TestTags.DISCARD_CANCEL),
                ) { Text("Keep editing") }
            },
        )
    }

    if (showDeleteDialog) {
        DeleteExpenseDialog(
            expenseTitle = state.form.title,
            onConfirm = { showDeleteDialog = false; viewModel.delete() },
            onCancel = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun ExpenseFormContent(
    state: EditUiState,
    viewModel: AddEditViewModel,
    modifier: Modifier = Modifier,
) {
    val form = state.form
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        OutlinedTextField(
            value = form.title,
            onValueChange = viewModel::onTitleChange,
            label = { Text("Title") },
            singleLine = true,
            isError = state.titleError != null,
            supportingText = {
                Row(Modifier.fillMaxWidth()) {
                    state.titleError?.let {
                        Text(it, modifier = Modifier.testTag(TestTags.ERROR_TITLE))
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${form.title.length}/${AddEditViewModel.TITLE_MAX}",
                        modifier = Modifier.testTag(TestTags.INPUT_TITLE_COUNTER),
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth().testTag(TestTags.INPUT_TITLE),
        )

        OutlinedTextField(
            value = form.amount,
            onValueChange = viewModel::onAmountChange,
            label = { Text("Amount") },
            prefix = { Text(viewModel.currency.symbol) },
            singleLine = true,
            isError = state.amountError != null,
            supportingText = state.amountError?.let { error ->
                { Text(error, modifier = Modifier.testTag(TestTags.ERROR_AMOUNT)) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth().testTag(TestTags.INPUT_AMOUNT),
        )

        CategoryPicker(
            selected = form.category,
            error = state.categoryError,
            onSelect = viewModel::onCategoryChange,
        )

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
                .testTag(TestTags.INPUT_DATE),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Date", style = MaterialTheme.typography.labelMedium)
                    Text(formatDate(form.date), modifier = Modifier.testTag(TestTags.INPUT_DATE_VALUE))
                }
                Icon(Icons.Filled.CalendarMonth, contentDescription = "Choose date")
            }
        }

        Text("Payment method", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.selectableGroup()) {
            PaymentMethod.entries.forEach { method ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .selectable(
                            selected = form.paymentMethod == method,
                            onClick = { viewModel.onPaymentMethodChange(method) },
                            role = Role.RadioButton,
                        )
                        .padding(end = 16.dp)
                        .testTag(TestTags.paymentOption(method)),
                ) {
                    RadioButton(selected = form.paymentMethod == method, onClick = null)
                    Spacer(Modifier.width(4.dp))
                    Text(method.label)
                }
            }
        }

        OutlinedTextField(
            value = form.note,
            onValueChange = viewModel::onNoteChange,
            label = { Text("Note (optional)") },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth().testTag(TestTags.INPUT_NOTE),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = form.reimbursable,
                    onValueChange = viewModel::onReimbursableChange,
                    role = Role.Checkbox,
                )
                .testTag(TestTags.CHECKBOX_REIMBURSABLE),
        ) {
            Checkbox(checked = form.reimbursable, onCheckedChange = null)
            Spacer(Modifier.width(8.dp))
            Text("Reimbursable by company")
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = viewModel::save,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag(TestTags.SAVE_BUTTON),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp).testTag(TestTags.SAVE_PROGRESS),
                )
                Spacer(Modifier.width(8.dp))
                Text("Saving…")
            } else {
                Text(if (state.isEdit) "Save changes" else "Save expense")
            }
        }
    }

    if (showDatePicker) {
        ExpenseDatePicker(
            date = form.date,
            onConfirm = { viewModel.onDateChange(it); showDatePicker = false },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun CategoryPicker(selected: Category?, error: String?, onSelect: (Category) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Box {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .testTag(TestTags.INPUT_CATEGORY),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                    selected?.let {
                        Icon(it.icon, contentDescription = null, tint = it.color)
                        Spacer(Modifier.width(12.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Category", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = selected?.label ?: "Choose a category",
                            color = if (selected == null) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag(TestTags.INPUT_CATEGORY_VALUE),
                        )
                    }
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.appiumRoot().testTag(TestTags.CATEGORY_MENU),
            ) {
                Category.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.label) },
                        leadingIcon = { Icon(category.icon, contentDescription = null, tint = category.color) },
                        onClick = { onSelect(category); expanded = false },
                        modifier = Modifier.testTag(TestTags.categoryOption(category)),
                    )
                }
            }
        }
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp).testTag(TestTags.ERROR_CATEGORY),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseDatePicker(date: LocalDate, onConfirm: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.appiumRoot().testTag(TestTags.DATE_PICKER_DIALOG),
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    } ?: onDismiss()
                },
                modifier = Modifier.testTag(TestTags.DATE_PICKER_OK),
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag(TestTags.DATE_PICKER_CANCEL)) {
                Text("Cancel")
            }
        },
    ) {
        DatePicker(state = pickerState)
    }
}
