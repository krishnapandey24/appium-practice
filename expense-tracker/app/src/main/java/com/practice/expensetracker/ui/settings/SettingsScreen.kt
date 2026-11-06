package com.practice.expensetracker.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.practice.expensetracker.data.Currency
import com.practice.expensetracker.data.SettingsStore
import com.practice.expensetracker.ui.common.AppSnackbarHost
import com.practice.expensetracker.ui.common.AppViewModelFactory
import com.practice.expensetracker.ui.common.TestTags
import com.practice.expensetracker.ui.common.appiumRoot
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class SettingsViewModel(
    val settings: SettingsStore,
    private val messages: Channel<String>,
) : ViewModel() {

    fun setCurrency(currency: Currency) = settings.setCurrency(currency)

    fun setConfirmDelete(value: Boolean) = settings.setConfirmDelete(value)

    /** Returns an error message, or null when the budget was saved. */
    fun saveBudget(input: String): String? {
        val value = input.toDoubleOrNull()
        val error = when {
            input.isBlank() -> "Enter a budget"
            value == null -> "Enter a valid number"
            value <= 0 -> "Budget must be greater than 0"
            else -> null
        }
        if (error == null) {
            settings.setBudget(value!!)
            viewModelScope.launch { messages.send("Budget updated") }
        }
        return error
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenDebug: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelFactory),
) {
    val currency by viewModel.settings.currency.collectAsStateWithLifecycle()
    val budget by viewModel.settings.budget.collectAsStateWithLifecycle()
    val confirmDelete by viewModel.settings.confirmDelete.collectAsStateWithLifecycle()
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var budgetInput by remember(budget) { mutableStateOf(budget.toLong().toString()) }
    var budgetError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { AppSnackbarHost() },
        topBar = { TopAppBar(title = { Text("Settings", modifier = Modifier.testTag(TestTags.SCREEN_TITLE)) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Preferences")
            ListItem(
                headlineContent = { Text("Currency") },
                supportingContent = {
                    Text(
                        "${currency.displayName} (${currency.symbol})",
                        modifier = Modifier.testTag(TestTags.SETTINGS_CURRENCY_VALUE),
                    )
                },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                modifier = Modifier
                    .clickable { showCurrencyDialog = true }
                    .testTag(TestTags.SETTINGS_CURRENCY),
            )
            HorizontalDivider()

            Column(Modifier.padding(16.dp)) {
                Text("Monthly budget", style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it; budgetError = null },
                        prefix = { Text(currency.symbol) },
                        singleLine = true,
                        isError = budgetError != null,
                        supportingText = budgetError?.let { error ->
                            { Text(error, modifier = Modifier.testTag(TestTags.SETTINGS_BUDGET_ERROR)) }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag(TestTags.SETTINGS_BUDGET_INPUT),
                    )
                    Button(
                        onClick = {
                            budgetError = viewModel.saveBudget(budgetInput)
                            if (budgetError == null) focusManager.clearFocus()
                        },
                        modifier = Modifier.padding(top = 8.dp).testTag(TestTags.SETTINGS_BUDGET_SAVE),
                    ) { Text("Save") }
                }
            }
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Confirm before deleting") },
                supportingContent = { Text("Ask before an expense is deleted") },
                trailingContent = { Switch(checked = confirmDelete, onCheckedChange = null) },
                modifier = Modifier
                    .toggleable(
                        value = confirmDelete,
                        onValueChange = viewModel::setConfirmDelete,
                        role = Role.Switch,
                    )
                    .testTag(TestTags.SETTINGS_CONFIRM_DELETE_SWITCH),
            )

            SectionHeader("Developer")
            ListItem(
                headlineContent = { Text("Debug tools") },
                supportingContent = { Text("Dummy data, slow mode, reset") },
                leadingContent = { Icon(Icons.Filled.BugReport, contentDescription = null) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                modifier = Modifier
                    .clickable(onClick = onOpenDebug)
                    .testTag(TestTags.SETTINGS_OPEN_DEBUG),
            )

            SectionHeader("About")
            ListItem(
                headlineContent = { Text("Version") },
                supportingContent = { Text("1.0 (1)") },
            )
        }
    }

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            modifier = Modifier.appiumRoot().testTag(TestTags.CURRENCY_DIALOG),
            title = { Text("Currency") },
            text = {
                Column(Modifier.selectableGroup()) {
                    Currency.entries.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = option == currency,
                                    onClick = {
                                        viewModel.setCurrency(option)
                                        showCurrencyDialog = false
                                    },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 12.dp)
                                .testTag(TestTags.currencyOption(option)),
                        ) {
                            RadioButton(selected = option == currency, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text("${option.displayName} (${option.symbol})")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text("Close") }
            },
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp),
    )
}
