package com.better.spark.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.better.spark.domain.model.RelapseJournalEntry
import com.better.spark.domain.model.Task
import com.better.spark.domain.repository.RelapseJournalRepository
import com.better.spark.domain.repository.TaskRepository
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import org.koin.compose.koinInject

@Composable
fun ProgressScreen() {
    val taskRepo = koinInject<TaskRepository>()
    val relapseRepo = koinInject<RelapseJournalRepository>()

    val tz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date
    val days = 14
    val dates = remember(today) { List(days) { i -> today.minus((days - 1 - i), DateTimeUnit.DAY) } }

    val combinedFlow = remember {
        combine(taskRepo.observeTasks(), relapseRepo.observeEntries()) { tasks, relapses ->
            computeProgress(tasks, relapses, dates)
        }
    }

    val progress by combinedFlow.collectAsState(initial = ProgressData.empty(dates))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Progress", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Last $days days",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Summary cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "Completions",
                value = "${progress.totalCompletions}",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Relapses",
                value = "${progress.totalRelapseEvents}",
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "Money saved",
                value = if (progress.totalMoneySaved > 0.0) "$${progress.totalMoneySaved.toInt()}" else "—",
                modifier = Modifier.weight(1f)
            )
            val mins = progress.totalTimeSavedMins
            val timeText = when {
                mins <= 0.0 -> "—"
                mins >= 60.0 -> "${(mins / 60.0).toInt()}h"
                else -> "${mins.toInt()}m"
            }
            StatCard(
                title = "Time saved",
                value = timeText,
                modifier = Modifier.weight(1f)
            )
        }

        // Chart (meets requirement: at least one chart/visual summary)
        ChartCard(
            title = "Relapses vs completions",
            subtitle = "Relapses (red) and task completions (primary) per day",
        ) {
            LineChart(
                xLabels = progress.dates.map { it.toString().takeLast(5) },
                series = listOf(
                    ChartSeries(
                        name = "Completions",
                        values = progress.completionsPerDay.map { it.toFloat() },
                        color = MaterialTheme.colorScheme.primary
                    ),
                    ChartSeries(
                        name = "Relapses",
                        values = progress.relapsesPerDay.map { it.toFloat() },
                        color = MaterialTheme.colorScheme.error
                    )
                )
            )
        }

        ChartCard(
            title = "Saved over time",
            subtitle = "Money (green) and time (blue) saved per day",
        ) {
            LineChart(
                xLabels = progress.dates.map { it.toString().takeLast(5) },
                series = listOf(
                    ChartSeries(
                        name = "Money",
                        values = progress.moneySavedPerDay.map { it.toFloat() },
                        color = Color(0xFF2E7D32)
                    ),
                    ChartSeries(
                        name = "Time",
                        values = progress.timeSavedMinsPerDay.map { it.toFloat() },
                        color = Color(0xFF1565C0)
                    )
                )
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

private data class ProgressData(
    val dates: List<kotlinx.datetime.LocalDate>,
    val completionsPerDay: List<Int>,
    val relapsesPerDay: List<Int>,
    val moneySavedPerDay: List<Double>,
    val timeSavedMinsPerDay: List<Double>,
    val totalCompletions: Int,
    val totalRelapseEvents: Int,
    val totalMoneySaved: Double,
    val totalTimeSavedMins: Double
) {
    companion object {
        fun empty(dates: List<kotlinx.datetime.LocalDate>) = ProgressData(
            dates = dates,
            completionsPerDay = List(dates.size) { 0 },
            relapsesPerDay = List(dates.size) { 0 },
            moneySavedPerDay = List(dates.size) { 0.0 },
            timeSavedMinsPerDay = List(dates.size) { 0.0 },
            totalCompletions = 0,
            totalRelapseEvents = 0,
            totalMoneySaved = 0.0,
            totalTimeSavedMins = 0.0
        )
    }
}

private fun computeProgress(
    tasks: List<Task>,
    relapses: List<RelapseJournalEntry>,
    dates: List<kotlinx.datetime.LocalDate>
): ProgressData {
    // Task completion history is stored as date strings for scheduled tasks (and some epoch for interval tasks).
    // We'll count "YYYY-MM-DD" entries as daily completions for the progress view.
    val historyDates = tasks
        .filter { !it.isBadHabit }
        .flatMap { it.completionHistory }
        .mapNotNull { s -> if (s.length == 10 && s[4] == '-' && s[7] == '-') s else null }

    val completionsPerDay = dates.map { d ->
        val key = d.toString()
        historyDates.count { it == key }
    }

    // Relapses can be recorded in two places:
    // - New relapse journal entries
    // - Legacy per-habit relapseData map (used by the Quitting dashboard + streak logic)
    //
    // Take max per-day to avoid double counting when both exist.
    val tz = TimeZone.currentSystemDefault()
    val journalRelapsesPerDay = dates.map { d ->
        relapses.count { it.timestamp.toLocalDateTime(tz).date == d }
    }

    val relapseDataCountsByDay: Map<String, Int> = tasks
        .filter { it.isBadHabit }
        .flatMap { habit ->
            habit.relapseData.entries
                .filter { (_, amount) -> amount > 0.0 }
                .map { (dateStr, _) -> dateStr }
        }
        .groupingBy { it }
        .eachCount()

    val legacyRelapsesPerDay = dates.map { d ->
        relapseDataCountsByDay[d.toString()] ?: 0
    }

    val relapsesPerDay = dates.indices.map { i ->
        maxOf(journalRelapsesPerDay[i], legacyRelapsesPerDay[i])
    }

    // Savings: sum across all active bad habits for each day as "snapshot at that day".
    // This yields a meaningful trend line without needing historic baselines.
    val badHabits = tasks.filter { it.isBadHabit }
    val moneySavedPerDay = dates.map { d -> badHabits.sumOf { it.getTotalMoneySaved(d) } }
    val timeSavedPerDay = dates.map { d -> badHabits.sumOf { it.getTotalTimeSaved(d) } }

    return ProgressData(
        dates = dates,
        completionsPerDay = completionsPerDay,
        relapsesPerDay = relapsesPerDay,
        moneySavedPerDay = moneySavedPerDay,
        timeSavedMinsPerDay = timeSavedPerDay,
        totalCompletions = completionsPerDay.sum(),
        totalRelapseEvents = relapsesPerDay.sum(),
        totalMoneySaved = moneySavedPerDay.lastOrNull() ?: 0.0,
        totalTimeSavedMins = timeSavedPerDay.lastOrNull() ?: 0.0
    )
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

private data class ChartSeries(
    val name: String,
    val values: List<Float>,
    val color: Color
)

@Composable
private fun LineChart(
    xLabels: List<String>,
    series: List<ChartSeries>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
) {
    val allValues = series.flatMap { it.values }
    val maxY = (allValues.maxOrNull() ?: 0f).coerceAtLeast(1f)

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val paddingLeft = 8.dp.toPx()
            val paddingTop = 8.dp.toPx()
            val paddingRight = 8.dp.toPx()
            val paddingBottom = 16.dp.toPx()

            val w = size.width - paddingLeft - paddingRight
            val h = size.height - paddingTop - paddingBottom
            val n = (xLabels.size).coerceAtLeast(2)

            // grid baseline
            drawLine(
                color = Color.Gray.copy(alpha = 0.15f),
                start = Offset(paddingLeft, paddingTop + h),
                end = Offset(paddingLeft + w, paddingTop + h),
                strokeWidth = 2f
            )

            series.forEach { s ->
                val path = Path()
                s.values.forEachIndexed { idx, v ->
                    val x = paddingLeft + (idx.toFloat() / (n - 1).toFloat()) * w
                    val y = paddingTop + h - (v / maxY) * h
                    if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = s.color,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

