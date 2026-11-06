package com.practice.expensetracker.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.practice.expensetracker.ui.common.color

/**
 * Charts are drawn on a Canvas, so Appium can't see the individual slices or bars.
 * The only thing exposed to automation is the content description summary.
 */
@Composable
fun DonutChart(slices: List<CategorySlice>, description: String, modifier: Modifier = Modifier, size: Dp = 160.dp) {
    val total = slices.sumOf { it.amount }.toFloat()
    val track = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier.size(size).semantics { contentDescription = description }) {
        val stroke = this.size.minDimension * 0.18f
        val diameter = this.size.minDimension - stroke
        val topLeft = Offset((this.size.width - diameter) / 2, (this.size.height - diameter) / 2)
        val arcSize = Size(diameter, diameter)
        if (total <= 0f) {
            drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
            return@Canvas
        }
        var startAngle = -90f
        slices.forEach { slice ->
            val sweep = (slice.amount.toFloat() / total) * 360f
            drawArc(slice.category.color, startAngle, sweep - 1.5f, false, topLeft, arcSize, style = Stroke(stroke))
            startAngle += sweep
        }
    }
}

@Composable
fun BarChart(entries: List<BarEntry>, description: String, modifier: Modifier = Modifier) {
    val max = (entries.maxOfOrNull { it.amount } ?: 0.0).coerceAtLeast(1.0)
    val barColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor)
    val labelEvery = if (entries.size > 12) 5 else 1

    Canvas(
        modifier
            .fillMaxWidth()
            .height(180.dp)
            .semantics { contentDescription = description },
    ) {
        val labelHeight = 16.dp.toPx()
        val chartHeight = size.height - labelHeight
        val slot = size.width / entries.size.coerceAtLeast(1)
        val barWidth = slot * 0.6f

        listOf(0f, 0.5f, 1f).forEach { f ->
            val y = chartHeight * (1 - f)
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }
        entries.forEachIndexed { index, entry ->
            val barHeight = (entry.amount / max).toFloat() * chartHeight
            val x = index * slot + (slot - barWidth) / 2
            if (barHeight > 0f) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, chartHeight - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                )
            }
            if (index % labelEvery == 0 || index == entries.lastIndex) {
                val layout = textMeasurer.measure(entry.label, labelStyle)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(index * slot + slot / 2 - layout.size.width / 2, chartHeight + 2.dp.toPx()),
                )
            }
        }
    }
}
