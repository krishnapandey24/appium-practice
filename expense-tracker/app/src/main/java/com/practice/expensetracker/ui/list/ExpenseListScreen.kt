package com.practice.expensetracker.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.practice.expensetracker.data.Category
import com.practice.expensetracker.data.Currency
import com.practice.expensetracker.data.Expense
import com.practice.expensetracker.ui.common.AppSnackbarHost
import com.practice.expensetracker.ui.common.AppViewModelFactory
import com.practice.expensetracker.ui.common.DeleteExpenseDialog
import com.practice.expensetracker.ui.common.TestTags
import com.practice.expensetracker.ui.common.appiumRoot
import com.practice.expensetracker.ui.common.color
import com.practice.expensetracker.ui.common.formatDate
import com.practice.expensetracker.ui.common.formatMoney
import com.practice.expensetracker.ui.common.icon
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    snackbarHostState: SnackbarHostState,
    onAddExpense: () -> Unit,
    onOpenExpense: (Long) -> Unit,
    onOpenDebug: () -> Unit,
    viewModel: ExpenseListViewModel = viewModel(factory = AppViewModelFactory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pendingDelete by viewModel.pendingDelete.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // New filters or sort order: show the list from the top.
    LaunchedEffect(state.filters) { listState.scrollToItem(0) }

    LaunchedEffect(Unit) {
        viewModel.deleted.collect { expense ->
            val result = snackbarHostState.showSnackbar(
                message = "Expense deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete(expense)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Expenses", modifier = Modifier.testTag(TestTags.SCREEN_TITLE)) },
                actions = { OverflowMenu(onLoadSample = viewModel::loadSampleData, onOpenDebug = onOpenDebug) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpense,
                modifier = Modifier.testTag(TestTags.ADD_EXPENSE_FAB),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add expense")
            }
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.testTag(TestTags.LOADING_INDICATOR))
            }
            return@Scaffold
        }

        Column(Modifier.padding(padding).fillMaxSize()) {
            SpentCard(spent = state.monthSpent, budget = state.budget, currency = state.currency)
            SearchField(query = state.filters.query, onQueryChange = viewModel::onQueryChange)
            PeriodAndSortRow(
                period = state.filters.period,
                sort = state.filters.sort,
                onPeriodChange = viewModel::onPeriodChange,
                onSortChange = viewModel::onSortChange,
            )
            CategoryChips(selected = state.filters.categories, onToggle = viewModel::toggleCategory)
            Text(
                text = "Showing ${state.expenses.size} expenses",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag(TestTags.EXPENSE_COUNT_TEXT),
            )

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag(TestTags.PULL_TO_REFRESH),
            ) {
                when {
                    state.totalInDb == 0 -> EmptyState(onAddExpense)
                    state.expenses.isEmpty() -> EmptyFilterState(viewModel::clearFilters)
                    else -> LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().testTag(TestTags.EXPENSE_LIST),
                        contentPadding = PaddingValues(bottom = 96.dp),
                    ) {
                        items(state.expenses, key = { it.id }) { expense ->
                            SwipeableExpenseRow(
                                expense = expense,
                                currency = state.currency,
                                onClick = { onOpenExpense(expense.id) },
                                onDelete = { viewModel.requestDelete(expense) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { expense ->
        DeleteExpenseDialog(
            expenseTitle = expense.title,
            onConfirm = viewModel::confirmDelete,
            onCancel = viewModel::cancelDelete,
        )
    }
}

@Composable
private fun OverflowMenu(onLoadSample: () -> Unit, onOpenDebug: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.testTag(TestTags.OVERFLOW_MENU_BUTTON),
        ) {
            Icon(Icons.Filled.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.appiumRoot().testTag(TestTags.OVERFLOW_MENU),
        ) {
            DropdownMenuItem(
                text = { Text("Load sample data") },
                onClick = { expanded = false; onLoadSample() },
                modifier = Modifier.testTag(TestTags.MENU_LOAD_SAMPLE),
            )
            DropdownMenuItem(
                text = { Text("Debug tools") },
                onClick = { expanded = false; onOpenDebug() },
                modifier = Modifier.testTag(TestTags.MENU_DEBUG),
            )
        }
    }
}

@Composable
private fun SpentCard(spent: Double, budget: Double, currency: Currency) {
    val fraction = if (budget > 0) spent / budget else 0.0
    val overBudget = spent > budget * 1.1
    val nearLimit = !overBudget && fraction >= 0.8
    val percent = (fraction * 100).roundToInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(TestTags.SPENT_CARD),
        colors = CardDefaults.cardColors(
            containerColor = if (overBudget) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Spent this month", style = MaterialTheme.typography.labelLarge)
            Text(
                text = formatMoney(spent, currency),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag(TestTags.SPENT_AMOUNT),
            )
            Text(
                text = "of ${formatMoney(budget, currency)} budget",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag(TestTags.BUDGET_TEXT),
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { fraction.toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().testTag(TestTags.BUDGET_PROGRESS),
                color = if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "$percent% used",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp).testTag(TestTags.BUDGET_PERCENT),
            )
        }
    }
    when {
        overBudget -> Banner(
            text = "Over budget by ${formatMoney(spent - budget, currency)}",
            tag = TestTags.BUDGET_OVER_BANNER,
            container = MaterialTheme.colorScheme.error,
            content = MaterialTheme.colorScheme.onError,
        )
        nearLimit -> Banner(
            text = "Heads up: you've used $percent% of this month's budget",
            tag = TestTags.BUDGET_WARNING_BANNER,
            container = Color(0xFFFFE8B0),
            content = Color(0xFF4A3500),
        )
    }
}

@Composable
private fun Banner(text: String, tag: String, container: Color, content: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(container, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag(tag),
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = content, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search by title or note") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.testTag(TestTags.SEARCH_CLEAR_BUTTON),
                ) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag(TestTags.SEARCH_FIELD),
    )
}

@Composable
private fun PeriodAndSortRow(
    period: PeriodFilter,
    sort: SortOption,
    onPeriodChange: (PeriodFilter) -> Unit,
    onSortChange: (SortOption) -> Unit,
) {
    var sortMenuOpen by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp),
    ) {
        PeriodFilter.entries.forEach { option ->
            FilterChip(
                selected = period == option,
                onClick = { onPeriodChange(option) },
                label = { Text(option.label) },
                modifier = Modifier.testTag(TestTags.periodChip(option.name)),
            )
        }
        Spacer(Modifier.weight(1f))
        Box {
            IconButton(
                onClick = { sortMenuOpen = true },
                modifier = Modifier.testTag(TestTags.SORT_BUTTON),
            ) {
                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
            }
            DropdownMenu(
                expanded = sortMenuOpen,
                onDismissRequest = { sortMenuOpen = false },
                modifier = Modifier.appiumRoot().testTag(TestTags.SORT_MENU),
            ) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = { sortMenuOpen = false; onSortChange(option) },
                        leadingIcon = {
                            if (option == sort) Icon(Icons.Filled.Check, contentDescription = "Selected")
                            else Spacer(Modifier.size(24.dp))
                        },
                        modifier = Modifier.testTag(TestTags.sortOption(option.name)),
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChips(selected: List<Category>, onToggle: (Category) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.fillMaxWidth().testTag(TestTags.CATEGORY_FILTER_ROW),
    ) {
        items(Category.entries) { category ->
            FilterChip(
                selected = category in selected,
                onClick = { onToggle(category) },
                label = { Text(category.label) },
                leadingIcon = { Icon(category.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag(TestTags.categoryChip(category)),
            )
        }
    }
}

@Composable
private fun SwipeableExpenseRow(
    expense: Expense,
    currency: Currency,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        onDismiss = {
            onDelete()
            scope.launch { dismissState.reset() }
        },
        backgroundContent = {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 24.dp),
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onError)
            }
        },
    ) {
        ListItem(
            modifier = Modifier
                .clickable(onClick = onClick)
                .testTag(TestTags.expenseItem(expense.id)),
            leadingContent = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .background(expense.category.color.copy(alpha = 0.18f), CircleShape),
                ) {
                    Icon(expense.category.icon, contentDescription = null, tint = expense.category.color)
                }
            },
            headlineContent = {
                Text(
                    text = expense.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag(TestTags.expenseTitle(expense.id)),
                )
            },
            supportingContent = {
                Column {
                    Row {
                        Text(expense.category.label, modifier = Modifier.testTag(TestTags.expenseCategory(expense.id)))
                        Text(" · ${expense.paymentMethod.label}")
                    }
                    Text(
                        text = formatDate(expense.date),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag(TestTags.expenseDate(expense.id)),
                    )
                }
            },
            trailingContent = {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatMoney(expense.amount, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag(TestTags.expenseAmount(expense.id)),
                    )
                    if (expense.reimbursable) {
                        Text(
                            text = "Reimbursable",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.testTag(TestTags.expenseReimbursable(expense.id)),
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun EmptyState(onAddExpense: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp)
            .testTag(TestTags.EMPTY_STATE),
    ) {
        Text("No expenses yet", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Add your first expense, or load sample data from the menu.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAddExpense, modifier = Modifier.testTag(TestTags.EMPTY_STATE_ADD_BUTTON)) {
            Text("Add expense")
        }
    }
}

@Composable
private fun EmptyFilterState(onClearFilters: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp)
            .testTag(TestTags.EMPTY_FILTER_STATE),
    ) {
        Text("No expenses match your filters", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onClearFilters, modifier = Modifier.testTag(TestTags.CLEAR_FILTERS_BUTTON)) {
            Text("Clear filters")
        }
    }
}
