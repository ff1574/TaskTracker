package com.better.spark.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.better.spark.domain.model.BadHabitType
import com.better.spark.domain.model.Task
import com.better.spark.presentation.model.TaskUiState
import com.better.spark.presentation.viewmodel.BadHabitViewModel
import com.better.spark.presentation.viewmodel.RelapseJournalViewModel
import com.better.spark.presentation.viewmodel.TaskViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    resetKey: Int = 0,
    onOpenLifeCalendar: (() -> Unit)? = null
) {
    val taskViewModel = koinViewModel<TaskViewModel>()
    val badHabitViewModel = koinViewModel<BadHabitViewModel>()
    val relapseJournalViewModel = koinViewModel<RelapseJournalViewModel>()

    val taskState by taskViewModel.uiState.collectAsState()
    val habitState by badHabitViewModel.uiState.collectAsState()

    val tz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date
    val todayIsoDow = today.dayOfWeek.ordinal + 1 // 1=Mon … 7=Sun
    val todayStr = today.toString()

    var relapseDialogTask by remember { mutableStateOf<Task?>(null) }
    val scrollState = rememberScrollState()

    LaunchedEffect(resetKey) {
        relapseDialogTask = null
        scrollState.scrollTo(0)
    }

    val tasks: List<Task> = (taskState as? TaskUiState.Success)?.tasks.orEmpty()
        .filter { !it.isArchived && !it.isBadHabit }
        .filterNot { it.isCompleted && !it.isRepeatable } // mirror TaskListScreen's "auto archive one-time" UX

    val habits: List<Task> = (habitState as? TaskUiState.Success)?.tasks.orEmpty()
        .filter { !it.isArchived && it.isBadHabit }

    val dueTodayTasks = remember(tasks, todayIsoDow) {
        tasks
            .filter { task ->
                when {
                    task.repeatInterval != null -> true
                    !task.repeatDays.isNullOrEmpty() -> task.repeatDays.contains(todayIsoDow)
                    else -> true // one-time tasks are always "due" until done
                }
            }
            .sortedWith(
                compareBy<Task> { it.isCompleted }
                    .thenBy { it.repeatInterval == null && it.repeatDays.isNullOrEmpty() } // habits first
                    .thenBy { it.title.lowercase() }
            )
            .take(6)
    }

    val (moneySaved, timeSavedMins) = remember(habits, today) {
        val money = habits.sumOf { it.getTotalMoneySaved(today) }
        val time = habits.sumOf { it.getTotalTimeSaved(today) }
        money to time
    }

    val recentRelapses = remember(habits) {
        habits
            .flatMap { habit ->
                habit.relapseData.entries.map { (dateStr, amount) ->
                    RelapseEvent(
                        habitId = habit.id,
                        habitTitle = habit.title,
                        habitType = habit.badHabitType,
                        dateStr = dateStr,
                        amount = amount
                    )
                }
            }
            .sortedByDescending { it.dateStr }
            .take(6)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = todayStr,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ── Quick summary ─────────────────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active bad habits", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("${habits.size}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Relapses today", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        val relapsesToday = habits.sumOf { (it.relapseData[todayStr] ?: 0.0) }.toInt()
                        Text("$relapsesToday", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Money saved", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (moneySaved > 0.0) "$${moneySaved.toInt()}" else "—",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Time saved", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val hours = (timeSavedMins / 60.0).toInt()
                        val minutes = (timeSavedMins % 60.0).toInt()
                        val timeText = when {
                            timeSavedMins <= 0.0 -> "—"
                            hours > 0 -> "${hours}h ${minutes}m"
                            else -> "${minutes}m"
                        }
                        Text(timeText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (onOpenLifeCalendar != null) {
                    OutlinedButton(onClick = onOpenLifeCalendar, modifier = Modifier.fillMaxWidth()) {
                        Text("Open Life Calendar")
                    }
                }
            }
        }

        // ── Today's tasks & habits ────────────────────────────────────────────
        SectionTitle(title = "Today", subtitle = "Quick complete / uncomplete")

        when (taskState) {
            is TaskUiState.Loading -> {
                Text("Loading tasks…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            is TaskUiState.Error -> {
                Text((taskState as TaskUiState.Error).message, color = MaterialTheme.colorScheme.error)
            }
            is TaskUiState.Success -> {
                if (dueTodayTasks.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("You’re all caught up.", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Add a task from the Tasks tab.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    dueTodayTasks.forEach { task ->
                        if (task.isRepeatable) {
                            RepeatableTaskItem(
                                task = task,
                                onToggle = { taskViewModel.toggleTaskComplete(task.id) },
                                onClick = { taskViewModel.toggleTaskComplete(task.id) }
                            )
                        } else {
                            TaskItem(
                                task = task,
                                onToggle = { taskViewModel.toggleTaskComplete(task.id) },
                                onClick = { taskViewModel.toggleTaskComplete(task.id) }
                            )
                        }
                    }
                }
            }
        }

        // ── Active bad habits + relapse action ────────────────────────────────
        SectionTitle(title = "Quitting", subtitle = "Active bad habits and relapse logging")

        when (habitState) {
            is TaskUiState.Loading -> Text("Loading bad habits…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            is TaskUiState.Error -> Text((habitState as TaskUiState.Error).message, color = MaterialTheme.colorScheme.error)
            is TaskUiState.Success -> {
                if (habits.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("No bad habits yet.", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Add one from the Quitting tab.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    habits.take(4).forEach { habit ->
                        BadHabitDashboardCard(
                            task = habit,
                            onRelapse = { relapseDialogTask = habit }
                        )
                    }
                }
            }
        }

        // ── Recent relapse activity ──────────────────────────────────────────
        if (recentRelapses.isNotEmpty()) {
            SectionTitle(title = "Recent relapse activity", subtitle = "From locally stored relapse amounts")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    recentRelapses.forEach { e ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(e.habitTitle, fontWeight = FontWeight.SemiBold)
                                Text(
                                    e.dateStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${formatRelapseAmount(e.amount)} ${unitForHabit(e.habitType)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // keep content above bottom bar
    }

    if (relapseDialogTask != null) {
        RelapseAmountDialog(
            task = relapseDialogTask!!,
            onDismiss = { relapseDialogTask = null },
            onConfirm = { amount, notes, triggers, mood, timestampMillis ->
                badHabitViewModel.toggleRelapse(relapseDialogTask!!.id, amount)
                relapseJournalViewModel.addEntry(
                    badHabitId = relapseDialogTask!!.id,
                    amount = amount,
                    timestampMillis = timestampMillis,
                    mood = mood,
                    triggers = triggers,
                    notes = notes
                )
                relapseDialogTask = null
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (!subtitle.isNullOrBlank()) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BadHabitDashboardCard(
    task: Task,
    onRelapse: () -> Unit
) {
    val tz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date
    val streakDays = task.getStreakDays(today)
    val savedMoney = task.getTotalMoneySaved(today).toInt()
    val savedTime = task.getTotalTimeSaved(today).toInt()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(task.title, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (streakDays == 1) "1 day clean" else "$streakDays days clean",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = onRelapse) {
                    Text("Log relapse")
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = if (savedMoney > 0) "Saved $${savedMoney}" else "Saved —",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val timeText = when {
                    savedTime <= 0 -> "Reclaimed —"
                    savedTime >= 60 -> "Reclaimed ${savedTime / 60}h"
                    else -> "Reclaimed ${savedTime}m"
                }
                Text(timeText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private data class RelapseEvent(
    val habitId: String,
    val habitTitle: String,
    val habitType: BadHabitType?,
    val dateStr: String,
    val amount: Double
)

private fun unitForHabit(type: BadHabitType?): String {
    return when (type) {
        BadHabitType.SMOKING -> "cigs"
        BadHabitType.ALCOHOL -> "drinks"
        BadHabitType.SCREEN_TIME -> "units"
        BadHabitType.CUSTOM, null -> "units"
    }
}

private fun formatRelapseAmount(amount: Double): String {
    val intVal = amount.toInt()
    return if (amount == intVal.toDouble()) intVal.toString() else "%.1f".format(amount)
}




