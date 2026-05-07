package com.better.spark.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.better.spark.domain.model.BadHabitType
import com.better.spark.domain.model.Task
import com.better.spark.presentation.model.TaskUiState
import com.better.spark.presentation.viewmodel.BadHabitViewModel
import com.better.spark.presentation.viewmodel.RelapseJournalViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadHabitsScreen(
    viewModel: BadHabitViewModel,
    onOpenJournal: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val relapseJournalViewModel = koinViewModel<RelapseJournalViewModel>()
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var taskToRelapse by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedTask = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                shape = CircleShape
            ) {
                Text("+", fontSize = 24.sp)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is TaskUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                
                is TaskUiState.Success -> {
                    val habits = state.tasks.filter { !it.isArchived }

                    if (habits.isEmpty()) {
                        EmptyHabitsState(modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(habits, key = { it.id }) { task ->
                                BadHabitItem(
                                    task = task,
                                    onRelapse = { 
                                        taskToRelapse = task
                                    },
                                    onOpenJournal = {
                                        onOpenJournal(task.id)
                                    },
                                    onClick = {
                                        selectedTask = task
                                        showDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
                
                is TaskUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    if (showDialog) {
        BadHabitDialog(
            task = selectedTask,
            onDismiss = { showDialog = false },
            onConfirm = { title, desc, icon, color, type, baseline, timeLost, cost, currency ->
                if (selectedTask != null) {
                    val updatedTask = selectedTask!!.copy(
                        title = title,
                        description = desc,
                        iconName = icon,
                        colorHex = color,
                        badHabitType = type,
                        badHabitBaseline = baseline,
                        timePerFailure = timeLost,
                        costPerFailure = cost,
                        costCurrency = currency
                    )
                    viewModel.updateBadHabit(updatedTask)
                } else {
                    val newTask = Task(
                        id = Clock.System.now().toEpochMilliseconds().toString(),
                        title = title,
                        description = desc,
                        isBadHabit = true,
                        badHabitType = type,
                        badHabitBaseline = baseline,
                        timePerFailure = timeLost,
                        costPerFailure = cost,
                        costCurrency = currency,
                        iconName = icon,
                        colorHex = color,
                        createdAt = Clock.System.now()
                    )
                    viewModel.addBadHabit(newTask)
                }
                showDialog = false
            },
            onDelete = {
                selectedTask?.let { viewModel.deleteBadHabit(it.id) }
                showDialog = false
            },
            onArchive = {
                selectedTask?.let { viewModel.archiveBadHabit(it.id) }
                showDialog = false
            }
        )
    }
    
    if (taskToRelapse != null) {
        RelapseAmountDialog(
            task = taskToRelapse!!,
            onDismiss = { taskToRelapse = null },
            onConfirm = { amount, notes, triggers, mood, timestampMillis ->
                viewModel.toggleRelapse(taskToRelapse!!.id, amount)
                relapseJournalViewModel.addEntry(
                    badHabitId = taskToRelapse!!.id,
                    amount = amount,
                    timestampMillis = timestampMillis,
                    mood = mood,
                    triggers = triggers,
                    notes = notes
                )
                taskToRelapse = null
            }
        )
    }
}

@Composable
private fun EmptyHabitsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✨", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Tame Your Demons",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            "Tap + to commit to quitting a bad habit",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bad Habit Item
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BadHabitItem(
    task: Task,
    onRelapse: () -> Unit,
    onOpenJournal: () -> Unit,
    onClick: () -> Unit
) {
    val tz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date
    
    // Core Domain logic from Phase 1
    val streakDays = task.getStreakDays(today)
    val taskColor = TaskAppearance.getColor(task.colorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(taskColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = TaskAppearance.getIcon(task.iconName),
                        contentDescription = task.iconName,
                        tint = taskColor
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title & Subtext
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Display Quantifiable Savings
                    if (task.badHabitType != null) {
                        val savedUnits = task.getTotalSavedUnits(today)
                        val savedMoney = task.getTotalMoneySaved(today)
                        val savedTimeMins = task.getTotalTimeSaved(today)

                        val savingsTexts = mutableListOf<String>()
                        
                        if (savedMoney > 0.0) {
                            val curr = task.costCurrency ?: "$"
                            savingsTexts.add("Saved: $curr${savedMoney.toInt()}")
                        }
                        
                        if (savedTimeMins > 0.0) {
                            val hours = (savedTimeMins / 60.0).toInt()
                            if (hours > 0) savingsTexts.add("Reclaimed: ${hours}h")
                            else savingsTexts.add("Reclaimed: ${savedTimeMins.toInt()}m")
                        }

                        if (savingsTexts.isNotEmpty()) {
                            Text(
                                text = savingsTexts.joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (task.description.isNotBlank()) {
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    } else if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Sobriety Days Counter Profile
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$streakDays",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = if (streakDays > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = if (streakDays == 1) "Day Clean" else "Days Clean",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Relapse Button
            OutlinedButton(
                onClick = onRelapse,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "I Relapsed 😞",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onOpenJournal,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Journal")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Red Chain Grid (120 Days: 20x6)
            RedChainGrid(task = task, today = today)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// The Red Chain
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RedChainGrid(task: Task, today: kotlinx.datetime.LocalDate) {
    val columns = 20
    val rows = 6
    val totalDots = columns * rows

    val historyEntries = task.completionHistory  // "YYYY-MM-DD"

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        for (row in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (col in 0 until columns) {
                    // Right-to-Left Snake
                    val orderFromRight = columns - 1 - col
                    val rowWithinCol = if (orderFromRight % 2 == 0) {
                        rows - 1 - row
                    } else {
                        row
                    }
                    val daysAgo = orderFromRight * rows + rowWithinCol
                    
                    val cellDate = today.minus(daysAgo, DateTimeUnit.DAY).toString()
                    val isRelapse = historyEntries.contains(cellDate)
                    
                    val bg = if (isRelapse) {
                        MaterialTheme.colorScheme.error  // Relapsed = Bright Red
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f) // Success = Dim Empty
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(bg)
                    )
                }
            }
        }
    }
}
