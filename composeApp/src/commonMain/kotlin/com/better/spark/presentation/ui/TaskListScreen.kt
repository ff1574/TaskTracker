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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.better.spark.domain.model.Task
import com.better.spark.presentation.model.TaskUiState
import com.better.spark.presentation.viewmodel.TaskViewModel
import kotlinx.datetime.toLocalDateTime

/**
 * Screen displaying the list of tasks.
 * Refactored from App.kt with improved UI polish.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel
) {
    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()
    // State for managing dialog visibility and editing
    // If selectedTask is not null, we are editing mode
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    selectedTask = null // Reset for new task
                    showDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
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
                    if (state.tasks.isEmpty()) {
                        EmptyState(modifier = Modifier.align(Alignment.Center))
                    } else {
                        TaskLazyList(
                            tasks = state.tasks,
                            onToggleComplete = { viewModel.toggleTaskComplete(it) },
                            onTaskClick = { task -> 
                                selectedTask = task
                                showDialog = true
                            }
                        )
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
        TaskDialog(
            task = selectedTask,
            onDismiss = { showDialog = false },
            onConfirm = { title, desc, interval, days ->
                if (selectedTask != null) {
                    // Update existing task
                    val updatedTask = selectedTask!!.copy(
                        title = title,
                        description = desc,
                        repeatInterval = interval,
                        repeatDays = days
                    )
                    viewModel.updateTask(updatedTask)
                } else {
                    // Create new task
                    viewModel.addTask(title, desc, interval, days)
                }
                showDialog = false
            },
            onDelete = {
                selectedTask?.let {
                    viewModel.deleteTask(it.id)
                }
                showDialog = false
            }
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Placeholder for Lottie
        Text(
            "🎉",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "All caught up!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            "Tap + to add a new task",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TaskLazyList(
    tasks: List<Task>,
    onToggleComplete: (String) -> Unit,
    onTaskClick: (Task) -> Unit
) {
    val repeatableTasks = tasks.filter { it.repeatInterval != null || (it.repeatDays != null && it.repeatDays.isNotEmpty()) }
    val oneTimeTasks = tasks.filter { it.repeatInterval == null && (it.repeatDays == null || it.repeatDays.isEmpty()) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (repeatableTasks.isNotEmpty()) {
            item {
                Text(
                    "Repeatable Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                )
            }
            items(repeatableTasks, key = { task -> task.id }) { task ->
                TaskItem(
                    task = task,
                    onToggle = { onToggleComplete(task.id) },
                    onClick = { onTaskClick(task) }
                )
            }
        }

        if (oneTimeTasks.isNotEmpty()) {
            item {
                Text(
                    "One-time Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                )
            }
            items(oneTimeTasks, key = { task -> task.id }) { task ->
                TaskItem(
                    task = task,
                    onToggle = { onToggleComplete(task.id) },
                    onClick = { onTaskClick(task) }
                )
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    // Minimalist Design: Solid colors, no transparency/glassmorphism
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }, // CLICKABLE MODIFIER ADDED HERE
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface, // Solid surface color
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Subtle shadow instead of border
        border = null // No border for cleaner look
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
                )
                
                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Repeat Status
                if (task.repeatInterval != null || (task.repeatDays != null && task.repeatDays.isNotEmpty())) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = RepeatIcon(),
                            contentDescription = "Repeatable",
                            modifier = Modifier.size(14.dp),
                            tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        val repeatText = if (task.isCompleted && task.completedAt != null) {
                            // Logic for displaying reset time or next day
                            if (task.repeatInterval != null) {
                                val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                                val completedTime = task.completedAt.toEpochMilliseconds()
                                val diff = (completedTime + task.repeatInterval) - now
                                
                                if (diff > 0) {
                                    val minutes = (diff / (1000 * 60)) % 60
                                    val hours = (diff / (1000 * 60 * 60))
                                    "Resets in ${if (hours > 0) "${hours}h " else ""}${minutes}m"
                                } else {
                                    "Resetting..."
                                }
                            } else {
                                // Day repeat reset text
                                if (task.repeatDays != null && task.repeatDays.isNotEmpty()) {
                                    val now = kotlinx.datetime.Clock.System.now()
                                    val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
                                    val today = now.toLocalDateTime(tz).dayOfWeek.ordinal + 1 // 1=Mon, 7=Sun
                                    
                                    // Find the next scheduled day
                                    // Filter for days greater than today
                                    val nextDay = task.repeatDays.sorted().firstOrNull { it > today }
                                        ?: task.repeatDays.minOrNull() // If none, wrap around to first day
                                        ?: today
                                        
                                    val daysUntil = if (nextDay > today) {
                                        nextDay - today
                                    } else {
                                        (7 - today) + nextDay
                                    }
                                    
                                    if (daysUntil == 1) "Resets tomorrow"
                                    else if (daysUntil == 0) "Resets today" // Should imply it's active unless completed today
                                    else "Resets in $daysUntil days"
                                } else {
                                    "Resets tomorrow"
                                }
                            }
                        } else {
                           if (task.repeatInterval != null) "Repeatable" else "Weekly"
                        }
                        
                        Text(
                            text = repeatText,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Minimalist Checkbox
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline,
                    checkmarkColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
fun TaskDialog(
    task: Task? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long?, List<Int>?) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    
    // Repeat Logic
    var isRepeatable by remember { mutableStateOf(task?.repeatInterval != null || (task?.repeatDays != null && task.repeatDays.isNotEmpty())) }
    var repeatType by remember { mutableStateOf(if (task?.repeatDays != null && task.repeatDays.isNotEmpty()) RepeatType.SPECIFIC_DAYS else RepeatType.INTERVAL) }
    
    // Interval State
    val initialInterval = task?.repeatInterval ?: (24 * 60 * 60 * 1000L) // Default 1 day
    val initialUnit = RepeatUnit.entries.firstOrNull { initialInterval % it.multiplierInMillis == 0L && initialInterval >= it.multiplierInMillis } ?: RepeatUnit.DAYS
    val initialValue = (initialInterval / initialUnit.multiplierInMillis).toString()

    var repeatValue by remember { mutableStateOf(if (task?.repeatInterval != null) initialValue else "1") }
    var repeatUnit by remember { mutableStateOf(if (task?.repeatInterval != null) initialUnit else RepeatUnit.DAYS) }
    
    // Days State
    // 1=Mon, 7=Sun
    val selectedDays = remember { mutableStateListOf<Int>().apply { 
        if (task?.repeatDays != null) addAll(task.repeatDays)
    }}
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = BackIcon(),
                        contentDescription = "Cancel"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (task == null) "New Task" else "Edit Task",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What needs to be done?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Repeat Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isRepeatable,
                        onCheckedChange = { isRepeatable = it }
                    )
                    Text("Repeat this task?")
                }
                
                if (isRepeatable) {
                    // Type Selection
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        FilterChip(
                            selected = repeatType == RepeatType.INTERVAL,
                            onClick = { repeatType = RepeatType.INTERVAL },
                            label = { Text("Interval") }
                        )
                        FilterChip(
                            selected = repeatType == RepeatType.SPECIFIC_DAYS,
                            onClick = { repeatType = RepeatType.SPECIFIC_DAYS },
                            label = { Text("Schedule") }
                        )
                    }
                    
                    if (repeatType == RepeatType.INTERVAL) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = repeatValue,
                                onValueChange = { if (it.all { char -> char.isDigit() }) repeatValue = it },
                                label = { Text("Every") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            // Simple Dropdown substitute
                            var expanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { expanded = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(repeatUnit.label)
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    RepeatUnit.entries.forEach { unit ->
                                        DropdownMenuItem(
                                            text = { Text(unit.label) },
                                            onClick = { 
                                                repeatUnit = unit
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Days Selector
                        Text("Select Days:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Simple Day Toggles
                            val days = listOf("M", "T", "W", "T", "F", "S", "S")
                            days.forEachIndexed { index, dayLabel ->
                                val dayNum = index + 1
                                val isSelected = selectedDays.contains(dayNum)
                                
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            if (isSelected) selectedDays.remove(dayNum) else selectedDays.add(dayNum)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayLabel,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            IconButton(
                onClick = {
                    if (title.isNotBlank()) {
                        var interval: Long? = null
                        var days: List<Int>? = null
                        
                        if (isRepeatable) {
                            if (repeatType == RepeatType.INTERVAL) {
                                val value = repeatValue.toLongOrNull() ?: 1L
                                interval = value * repeatUnit.multiplierInMillis
                            } else {
                                days = selectedDays.toList()
                            }
                        }
                        
                        onConfirm(title.trim(), description.trim(), interval, days)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Icon(
                    imageVector = CheckIcon(),
                    contentDescription = "Confirm",
                    tint = if (title.isNotBlank()) Color(0xFF4CAF50) else Color.Gray // Green Checkmark
                )
            }
        },
        dismissButton = {
            if (task != null && onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = DeleteIcon(),
                        contentDescription = "Delete Task",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(28.dp)
    )
}

enum class RepeatType {
    INTERVAL, SPECIFIC_DAYS
}

enum class RepeatUnit(val label: String, val multiplierInMillis: Long) {
    MINUTES("Minutes", 60 * 1000L),
    HOURS("Hours", 60 * 60 * 1000L),
    DAYS("Days", 24 * 60 * 60 * 1000L),
    WEEKS("Weeks", 7 * 24 * 60 * 60 * 1000L)
}
