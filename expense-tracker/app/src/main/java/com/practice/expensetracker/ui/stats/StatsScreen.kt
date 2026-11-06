package com.practice.expensetracker.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.practice.expensetracker.data.Currency
import com.practice.expensetracker.ui.common.AppViewModelFactory
import com.practice.expensetracker.ui.common.TestTags
import com.practice.expensetracker.ui.common.color
import com.practice.expensetracker.ui.common.formatDate
import com.practice.expensetracker.ui.common.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = viewModel(factory = AppViewModelFactory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { TopAppBar(title = { Text("Insights", modifier = Modifier.testTag(TestTags.SCREEN_TITLE)) }) },
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatsPeriod.entries.forEach { p ->
                    FilterChip(
                        selected = state.period == p,
                        onClick = { viewModel.onPeriodChange(p) },
                        label = { Text(p.label) },
                        modifier = Modifier.testTag(TestTags.statsPeriod(p.name)),
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard("Total spent", formatMoney(state.total, state.currency), TestTags.STATS_TOTAL, Modifier.weight(1f))
                SummaryCard("Transactions", state.count.toString(), TestTags.STATS_COUNT, Modifier.weight(1f))
            }
            SummaryCard("Average per day", formatMoney(state.avgPerDay, state.currency), TestTags.STATS_AVG_PER_DAY, Modifier.fillMaxWidth())

            if (state.count == 0) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No expenses in this period",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag(TestTags.STATS_EMPTY),
                    )
                }
                return@Column
            }

            SectionCard("Spending by category") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DonutChart(
                        slices = state.slices,
                        description = "Spending by category: " +
                            state.slices.joinToString { "${it.category.label} ${it.percent}%" },
                        modifier = Modifier.testTag(TestTags.PIE_CHART),
                        size = 140.dp,
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.slices.forEach { slice -> LegendRow(slice, state.currency) }
                    }
                }
            }

            SectionCard(if (state.period == StatsPeriod.THIS_MONTH) "Daily spending" else "Monthly spending") {
                BarChart(
                    entries = state.bars,
                    description = "Bar chart of spending: " +
                        state.bars.filter { it.amount > 0 }.joinToString { "${it.label}: ${formatMoney(it.amount, state.currency)}" },
                    modifier = Modifier.testTag(TestTags.BAR_CHART),
                )
            }

            state.highest?.let { highest ->
                SectionCard("Biggest expense") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(highest.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${highest.category.label} · ${formatDate(highest.date)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            formatMoney(highest.amount, state.currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, tag: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag(tag),
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun LegendRow(slice: CategorySlice, currency: Currency) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.testTag(TestTags.legendItem(slice.category)),
    ) {
        Box(Modifier.size(10.dp).background(slice.category.color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(slice.category.label, style = MaterialTheme.typography.bodySmall)
            Row {
                Text(
                    formatMoney(slice.amount, currency),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.testTag(TestTags.legendAmount(slice.category)),
                )
                Text(" · ", style = MaterialTheme.typography.labelSmall)
                Text(
                    "${slice.percent}%",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.testTag(TestTags.legendPercent(slice.category)),
                )
            }
        }
    }
}
