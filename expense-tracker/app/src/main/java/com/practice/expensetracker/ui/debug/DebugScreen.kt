package com.practice.expensetracker.ui.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.practice.expensetracker.data.ExpenseRepository
import com.practice.expensetracker.data.SampleData
import com.practice.expensetracker.data.SettingsStore
import com.practice.expensetracker.ui.common.AppViewModelFactory
import com.practice.expensetracker.ui.common.TestTags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DebugViewModel(
    private val repository: ExpenseRepository,
    val settings: SettingsStore,
) : ViewModel() {

    val count: StateFlow<Int> = repository.count
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _status = MutableStateFlow("Ready")
    val status: StateFlow<String> = _status.asStateFlow()

    fun generate(input: String) {
        val requested = input.toIntOrNull()
        if (requested == null || requested !in 1..500) {
            _status.value = "Enter a number between 1 and 500"
            return
        }
        viewModelScope.launch {
            val items = (1 until requested).map { SampleData.random() }
            repository.insertAll(items)
            _status.value = "Generated $requested expenses"
        }
    }

    fun addRandom() {
        viewModelScope.launch {
            val expense = SampleData.random()
            repository.insertAll(listOf(expense))
            _status.value = "Added \"${expense.title}\""
        }
    }

    fun loadSample() {
        viewModelScope.launch {
            repository.deleteAll()
            val items = SampleData.fixed()
            repository.insertAll(items)
            _status.value = "Loaded sample dataset (${items.size} expenses)"
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.deleteAll()
            _status.value = "All expenses deleted"
        }
    }

    fun resetSettings() {
        settings.reset()
        _status.value = "Settings reset to defaults"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    onBack: () -> Unit,
    viewModel: DebugViewModel = viewModel(factory = AppViewModelFactory),
) {
    val count by viewModel.count.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val slowMode by viewModel.settings.slowMode.collectAsStateWithLifecycle()
    var countInput by remember { mutableStateOf("20") }
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Debug tools", modifier = Modifier.testTag(TestTags.SCREEN_TITLE)) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag(TestTags.BACK_BUTTON)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Expenses in database", style = MaterialTheme.typography.labelMedium)
                    Text(
                        count.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.testTag(TestTags.DEBUG_EXPENSE_COUNT),
                    )
                    Text(
                        status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag(TestTags.DEBUG_STATUS_TEXT),
                    )
                }
            }

            Text("Dummy data", style = MaterialTheme.typography.titleSmall)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = countInput,
                    onValueChange = { countInput = it.filter(Char::isDigit).take(3) },
                    label = { Text("How many") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag(TestTags.DEBUG_COUNT_INPUT),
                )
                Button(
                    onClick = { viewModel.generate(countInput) },
                    modifier = Modifier.testTag(TestTags.DEBUG_GENERATE_BUTTON),
                ) { Text("Generate") }
            }
            Text(
                "Random expenses from the last 90 days.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
                onClick = viewModel::addRandom,
                modifier = Modifier.fillMaxWidth().testTag(TestTags.DEBUG_ADD_RANDOM),
            ) { Text("Add 1 random expense") }
            OutlinedButton(
                onClick = viewModel::loadSample,
                modifier = Modifier.fillMaxWidth().testTag(TestTags.DEBUG_LOAD_SAMPLE),
            ) { Text("Replace with sample dataset (16 expenses)") }

            Text("Behaviour", style = MaterialTheme.typography.titleSmall)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = slowMode,
                        onValueChange = viewModel.settings::setSlowMode,
                        role = Role.Switch,
                    )
                    .testTag(TestTags.DEBUG_SLOW_MODE_SWITCH),
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Slow mode")
                    Text(
                        "Adds a 2.5s delay when the list loads and 1.5s when saving",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = slowMode, onCheckedChange = null)
            }

            Text("Reset", style = MaterialTheme.typography.titleSmall)
            OutlinedButton(
                onClick = viewModel::resetSettings,
                modifier = Modifier.fillMaxWidth().testTag(TestTags.DEBUG_RESET_SETTINGS),
            ) { Text("Reset settings to defaults") }
            Button(
                onClick = { showClearDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().testTag(TestTags.DEBUG_CLEAR_ALL),
            ) { Text("Delete all expenses") }
        }
    }

    // Deliberately left without test tags: practise locating dialog buttons by text.
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Delete all expenses?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { showClearDialog = false; viewModel.clearAll() }) {
                    Text("Delete everything", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            },
        )
    }
}
