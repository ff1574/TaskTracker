package com.better.spark.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*

/**
 * Screen displaying the list of tasks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onOpenTemplates: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showArchiveSheet by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Archive FAB — bottom left
                FloatingActionButton(
                    onClick = { showArchiveSheet = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = ArchiveIcon(),
                        contentDescription = "Archive",
                        modifier = Modifier.size(22.dp)
                    )
                }
                // Add Task FAB — bottom right (existing)
                FloatingActionButton(
                    onClick = {
                        selectedTask = null
                        showDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Text("+", fontSize = 24.sp)
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
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
                    // Completed one-time tasks auto-move to archive — hide them here
                    val visibleTasks = state.tasks.filter { task ->
                        !task.isArchived && !(task.isCompleted && !task.isRepeatable)
                    }
                    // Auto-archive newly completed one-time tasks
                    state.tasks
                        .filter { it.isCompleted && !it.isRepeatable && !it.isArchived }
                        .forEach { viewModel.archiveTask(it.id) }

                    if (visibleTasks.isEmpty()) {
                        EmptyState(modifier = Modifier.align(Alignment.Center))
                    } else {
                        TaskLazyList(
                            tasks = visibleTasks,
                            onToggleComplete = { viewModel.toggleTaskComplete(it) },
                            onTaskClick = { task ->
                                selectedTask = task
                                showDialog = true
                            }
                        )
                    }

                    // Templates entry point (always visible at bottom)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenTemplates,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Browse templates")
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
        TaskDialog(
            task = selectedTask,
            onDismiss = { showDialog = false },
            onConfirm = { title, desc, interval, days, icon, color, repeatCount, history ->
                if (selectedTask != null) {
                    val updatedTask = selectedTask!!.copy(
                        title = title,
                        description = desc,
                        repeatInterval = interval,
                        repeatDays = days,
                        iconName = icon,
                        colorHex = color,
                        repeatCount = repeatCount,
                        completionHistory = history
                    )
                    viewModel.updateTask(updatedTask)
                } else {
                    viewModel.addTask(title, desc, interval, days, icon, color, repeatCount)
                }
                showDialog = false
            },
            onDelete = {
                selectedTask?.let { viewModel.deleteTask(it.id) }
                showDialog = false
            },
            onArchive = if (selectedTask?.isRepeatable == true) ({
                selectedTask?.let { viewModel.archiveTask(it.id) }
                showDialog = false
            }) else null
        )
    }

    if (showArchiveSheet) {
        ArchiveSheet(
            viewModel = viewModel,
            onDismiss = { showArchiveSheet = false }
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
        Text("🎉", fontSize = 64.sp)
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
    val repeatableTasks = tasks.filter { it.isRepeatable }
    val oneTimeTasks = tasks.filter { !it.isRepeatable }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Repeatable Section ────────────────────────────────────────────
        if (repeatableTasks.isNotEmpty()) {
            item {
                SectionHeader(
                    label = "Habits & Routines",
                    count = repeatableTasks.size,
                    accent = MaterialTheme.colorScheme.primary
                )
            }
            items(repeatableTasks, key = { it.id }) { task ->
                RepeatableTaskItem(
                    task = task,
                    onToggle = { onToggleComplete(task.id) },
                    onClick = { onTaskClick(task) }
                )
            }
        }

        // Divider between sections
        if (repeatableTasks.isNotEmpty() && oneTimeTasks.isNotEmpty()) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // ── One-Time Section ──────────────────────────────────────────────
        if (oneTimeTasks.isNotEmpty()) {
            item {
                SectionHeader(
                    label = "One-Time Tasks",
                    count = oneTimeTasks.size,
                    accent = MaterialTheme.colorScheme.secondary
                )
            }
            items(oneTimeTasks, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    onToggle = { onToggleComplete(task.id) },
                    onClick = { onTaskClick(task) }
                )
            }
        }
    }
}

/**
 * Section header with count badge, consistent with the premium feel.
 */
@Composable
private fun SectionHeader(label: String, count: Int, accent: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 20.dp)
                .background(accent, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            shape = CircleShape,
            color = accent.copy(alpha = 0.15f)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accent,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 30-Day Streak Dots
// ─────────────────────────────────────────────────────────────────────────────

private sealed class DotState {
    /** No completion in this window/day. */
    object Missed : DotState()
    /**
     * fraction in (0, 1]: proportion of required completions achieved.
     * 1.0 = fully done, 0.5 = half done (shown at 50% opacity).
     */
    data class Done(val fraction: Float) : DotState()
}

/**
 * Builds the 30 streak dots for a task.
 *
 * - **Interval tasks**: Each dot = one interval window. History entries are epoch-millis strings.
 *   The most recent (rightmost) window is the current active period — shown as null/gray if not yet
 *   completed. Past windows with no completion → MISSED. Current window not yet done → null (in-progress).
 *
 * - **Specific-day tasks**: Scan back through calendar days collecting only scheduled days.
 *   History entries are YYYY-MM-DD date strings.
 */
private fun buildStreakDots(task: Task, targetCount: Int = 30): List<DotState?> {
    return if (task.repeatInterval != null) {
        buildIntervalDots(task, targetCount)
    } else {
        buildDayDots(task, targetCount)
    }
}

private fun buildIntervalDots(task: Task, targetCount: Int): List<DotState?> {
    val interval = task.repeatInterval ?: return emptyList()
    val nowMillis = Clock.System.now().toEpochMilliseconds()

    // Parse history as sorted epoch-millis longs
    val historyMillis = task.completionHistory.mapNotNull { it.toLongOrNull() }.sorted()

    // Anchor windows to the task's creation time so they align with the repeat schedule.
    // Window n starts at: createdAt + n*interval
    val anchorMillis = task.createdAt.toEpochMilliseconds()

    // Find how many complete windows have elapsed since creation
    val totalElapsed = ((nowMillis - anchorMillis) / interval).toInt().coerceAtLeast(0)

    // We show the last [targetCount] windows (oldest first)
    val firstWindowIndex = (totalElapsed - targetCount + 1).coerceAtLeast(0)

    val result = mutableListOf<DotState?>()
    for (n in firstWindowIndex..totalElapsed) {
        val windowStart = anchorMillis + n.toLong() * interval
        val windowEnd   = windowStart + interval

        val completedInWindow = historyMillis.any { it in windowStart until windowEnd }

        result.add(
            when {
                completedInWindow   -> DotState.Done(1f)
                windowEnd > nowMillis -> null              // current active window
                else                -> DotState.Missed
            }
        )
    }
    // Pad to targetCount if task is newer than targetCount windows
    while (result.size < targetCount) result.add(0, null)
    return result
}

private fun buildDayDots(task: Task, targetCount: Int): List<DotState?> {
    val tz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date
    val historyList = task.completionHistory  // may have duplicate date strings
    val rc = task.repeatCount.coerceAtLeast(1)
    val result = mutableListOf<DotState?>()

    var daysAgo = 0
    while (result.size < targetCount && daysAgo < 365) {
        val date = today.minus(daysAgo, DateTimeUnit.DAY)
        val dayIso = date.dayOfWeek.ordinal + 1 // 1=Mon, 7=Sun

        if (!task.repeatDays.isNullOrEmpty() && task.repeatDays.contains(dayIso)) {
            val dateStr = date.toString()
            val count = historyList.count { it == dateStr }
            val state: DotState? = when {
                count > 0   -> DotState.Done((count.toFloat() / rc).coerceIn(0f, 1f))
                daysAgo == 0 -> null          // today is still in-progress, same as interval's active window
                else         -> DotState.Missed
            }
            result.add(0, state)
        }
        daysAgo++
    }
    return result
}


@Composable
private fun StreakDots(task: Task) {
    val columns = 20
    val rows = 6
    val totalDots = columns * rows

    // Tick every 10 s so missed intervals become visible in real-time
    // without requiring the user to interact with the task first.
    var tick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10_000)
            tick++
        }
    }

    val dots = remember(task.completionHistory, task.repeatDays, task.repeatInterval, tick) {
        buildStreakDots(task, totalDots)
    }
    val taskColor = TaskAppearance.getColor(task.colorHex)
    val dotShape = RoundedCornerShape(2.dp)

    // 20 columns x 6 rows; most recent = top-right, oldest = bottom-left.
    // Pad front with empty slots if fewer than totalDots scheduled days exist yet.
    val padded = List(totalDots - dots.size) { null } + dots

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
                    // Boustrophedon (snake) pattern:
                    // Rightmost col (9) fills top→bottom; col 8 fills bottom→top; alternates.
                    // This ensures smooth transitions: bottom of one col connects to bottom of next.
                    val orderFromRight = columns - 1 - col
                    val rowWithinCol = if (orderFromRight % 2 == 0) {
                        rows - 1 - row  // even-from-right: top=newest, bottom=oldest
                    } else {
                        row             // odd-from-right: bottom=newest, top=oldest
                    }
                    val dotIndex = col * rows + rowWithinCol
                    val state = padded.getOrNull(dotIndex)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(dotShape)
                            .background(
                                when (val s = state) {
                                    is DotState.Done -> taskColor.copy(alpha = s.fraction)
                                    else             -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                }
                            )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Repeatable Task Item  (has streak dots + distinct left border)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RepeatableTaskItem(
    task: Task,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val taskColor = TaskAppearance.getColor(task.colorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, taskColor.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(start = 0.dp)) {
            // Thin colored top accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(taskColor, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(taskColor.copy(alpha = if (task.isCompleted) 0.15f else 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = TaskAppearance.getIcon(task.iconName),
                        contentDescription = task.iconName,
                        tint = if (task.isCompleted) taskColor.copy(alpha = 0.4f) else taskColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        color = if (task.isCompleted)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.onSurface
                    )

                    if (task.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Reset/repeat text
                    val repeatText = getRepeatSubtitle(task)
                    if (repeatText != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = RepeatIcon(),
                                contentDescription = "Repeatable",
                                modifier = Modifier.size(12.dp),
                                tint = if (task.isCompleted) taskColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = repeatText,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (task.isCompleted) taskColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 30-day streak dots grid
                    StreakDots(task = task)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Progress counter + checkbox
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val rc = task.repeatCount.coerceAtLeast(1)
                    if (rc > 1 && !task.repeatDays.isNullOrEmpty()) {
                        val tz = TimeZone.currentSystemDefault()
                        val today = Clock.System.now().toLocalDateTime(tz).date.toString()
                        val done = task.completionHistory.count { it == today }.coerceAtMost(rc)
                        Text(
                            "$done/$rc",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (task.isCompleted) taskColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onToggle() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = taskColor,
                            uncheckedColor = MaterialTheme.colorScheme.outline,
                            checkmarkColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }
    }
}


private fun getRepeatSubtitle(task: Task): String? {
    if (!task.isRepeatable) return null
    if (!task.isCompleted || task.completedAt == null) {
        return if (task.repeatInterval != null) "Repeatable" else "Weekly"
    }
    // Completed — show when it resets
    if (task.repeatInterval != null) {
        val now = Clock.System.now().toEpochMilliseconds()
        val completedTime = task.completedAt.toEpochMilliseconds()
        val diff = (completedTime + task.repeatInterval) - now
        if (diff > 0) {
            val minutes = (diff / (1000 * 60)) % 60
            val hours = (diff / (1000 * 60 * 60))
            return "Resets in ${if (hours > 0) "${hours}h " else ""}${minutes}m"
        }
        return "Resetting..."
    }
    if (!task.repeatDays.isNullOrEmpty()) {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = now.toLocalDateTime(tz).dayOfWeek.ordinal + 1
        val nextDay = task.repeatDays.sorted().firstOrNull { it > today }
            ?: task.repeatDays.minOrNull()
            ?: today
        val daysUntil = if (nextDay > today) nextDay - today else (7 - today) + nextDay
        return when (daysUntil) {
            0 -> "Resets today"
            1 -> "Resets tomorrow"
            else -> "Resets in $daysUntil days"
        }
    }
    return null
}

// ─────────────────────────────────────────────────────────────────────────────
// One-Time Task Item  (unchanged look, no dots)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TaskItem(
    task: Task,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TaskAppearance.getColor(task.colorHex).copy(alpha = if (task.isCompleted) 0.1f else 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = TaskAppearance.getIcon(task.iconName),
                    contentDescription = task.iconName,
                    tint = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f) else TaskAppearance.getColor(task.colorHex)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
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
            }

            Spacer(modifier = Modifier.width(16.dp))

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

// ─────────────────────────────────────────────────────────────────────────────
// TaskDialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TaskDialog(
    task: Task? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long?, List<Int>?, String, String, Int, List<String>) -> Unit,
    onDelete: (() -> Unit)? = null,
    onArchive: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }

    var selectedIcon by remember { mutableStateOf(task?.iconName ?: "ListChecks") }
    var selectedColor by remember { mutableStateOf(task?.colorHex ?: "#2196F3") }

    var showColorPicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    var isRepeatable by remember { mutableStateOf(task?.repeatInterval != null || (task?.repeatDays != null && task.repeatDays.isNotEmpty())) }
    var repeatType by remember { mutableStateOf(if (task?.repeatDays != null && task.repeatDays.isNotEmpty()) RepeatType.SPECIFIC_DAYS else RepeatType.INTERVAL) }

    val initialInterval = task?.repeatInterval ?: (24 * 60 * 60 * 1000L)
    val initialUnit = RepeatUnit.entries.firstOrNull { initialInterval % it.multiplierInMillis == 0L && initialInterval >= it.multiplierInMillis } ?: RepeatUnit.DAYS
    val initialValue = (initialInterval / initialUnit.multiplierInMillis).toString()

    var repeatValue by remember { mutableStateOf(if (task?.repeatInterval != null) initialValue else "1") }
    var repeatUnit by remember { mutableStateOf(if (task?.repeatInterval != null) initialUnit else RepeatUnit.DAYS) }
    var repeatCount by remember { mutableIntStateOf(task?.repeatCount?.coerceAtLeast(1) ?: 1) }

    val selectedDays = remember { mutableStateListOf<Int>().apply {
        if (task?.repeatDays != null) addAll(task.repeatDays)
    }}

    // Editable copy of completion history for retroactive edits
    val editableHistory = remember { mutableStateListOf<String>().apply {
        addAll(task?.completionHistory ?: emptyList())
    }}

    // Drives the separate PastCompletionsDialog
    var showPastCompletions by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = BackIcon(), contentDescription = "Cancel")
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
                
                // Appearance Selection
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = { showColorPicker = true },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(TaskAppearance.getColor(selectedColor)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Color")
                        }
                    }
                    
                    OutlinedButton(
                        onClick = { showIconPicker = true },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = TaskAppearance.getIcon(selectedIcon), contentDescription = "Selected Icon", tint = TaskAppearance.getColor(selectedColor))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Icon")
                        }
                    }
                }
                
                // Repeat Options
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isRepeatable, onCheckedChange = { isRepeatable = it })
                    Text("Repeat this task?")
                }
                
                if (isRepeatable) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        FilterChip(selected = repeatType == RepeatType.INTERVAL, onClick = { repeatType = RepeatType.INTERVAL }, label = { Text("Interval") })
                        FilterChip(selected = repeatType == RepeatType.SPECIFIC_DAYS, onClick = { repeatType = RepeatType.SPECIFIC_DAYS }, label = { Text("Schedule") })
                    }
                    
                    if (repeatType == RepeatType.INTERVAL) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = repeatValue,
                                onValueChange = { if (it.all { char -> char.isDigit() }) repeatValue = it },
                                label = { Text("Every") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            var expanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(onClick = { expanded = true }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text(repeatUnit.label)
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    RepeatUnit.entries.forEach { unit ->
                                        DropdownMenuItem(text = { Text(unit.label) }, onClick = { repeatUnit = unit; expanded = false })
                                    }
                                }
                            }
                        }
                    } else {
                        Text("Select Days:", style = MaterialTheme.typography.labelMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            val days = listOf("M", "T", "W", "T", "F", "S", "S")
                            days.forEachIndexed { index, dayLabel ->
                                val dayNum = index + 1
                                val isSelected = selectedDays.contains(dayNum)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { if (isSelected) selectedDays.remove(dayNum) else selectedDays.add(dayNum) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = dayLabel, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        // Times-per-day stepper
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Times per day:", style = MaterialTheme.typography.labelMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (repeatCount > 1) repeatCount-- },
                                    modifier = Modifier.size(32.dp)
                                ) { Text("-", style = MaterialTheme.typography.titleMedium) }
                                Text(
                                    "$repeatCount",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.widthIn(min = 24.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                IconButton(
                                    onClick = { if (repeatCount < 10) repeatCount++ },
                                    modifier = Modifier.size(32.dp)
                                ) { Text("+", style = MaterialTheme.typography.titleMedium) }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            // Full-width icon bar: Delete | Archive | Past completions | Confirm
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete
                IconButton(
                    onClick = { onDelete?.invoke() },
                    enabled = task != null && onDelete != null
                ) {
                    Icon(
                        imageVector = DeleteIcon(),
                        contentDescription = "Delete Task",
                        tint = if (task != null && onDelete != null)
                                   MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
                // Archive
                IconButton(
                    onClick = { onArchive?.invoke() },
                    enabled = task != null && onArchive != null
                ) {
                    Icon(
                        imageVector = ArchiveIcon(),
                        contentDescription = "Archive Task",
                        tint = if (task != null && onArchive != null)
                                   MaterialTheme.colorScheme.secondary
                               else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
                // Past completions (only enabled for specific-day tasks)
                val hasPastCompletions = task != null &&
                    task.repeatDays != null && task.repeatDays.isNotEmpty()
                IconButton(
                    onClick = { if (hasPastCompletions) showPastCompletions = true },
                    enabled = hasPastCompletions
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.CalendarDots,
                        contentDescription = "Edit past completions",
                        tint = if (hasPastCompletions)
                                   MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
                // Confirm
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
                            val rc = if (isRepeatable && repeatType == RepeatType.SPECIFIC_DAYS) repeatCount else 1
                            onConfirm(title.trim(), description.trim(), interval, days, selectedIcon, selectedColor, rc, editableHistory.toList())
                        }
                    },
                    enabled = title.isNotBlank()
                ) {
                    Icon(
                        imageVector = CheckIcon(),
                        contentDescription = "Confirm",
                        tint = if (title.isNotBlank()) Color(0xFF4CAF50) else Color.Gray
                    )
                }
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(28.dp)
    )
    
    // Color Picker Modal
    if (showColorPicker) {
        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text("Choose Color") },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                ) {
                    items(TaskAppearance.colors, key = { it }) { hex ->
                        val color = TaskAppearance.getColor(hex)
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = hex; showColorPicker = false },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == hex) {
                                Icon(imageVector = CheckIcon(), contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showColorPicker = false }) { Text("Close") } }
        )
    }
    
    // Icon Picker Modal  — with search
    if (showIconPicker) {
        var iconSearch by remember { mutableStateOf("") }
        val filteredIcons = remember(iconSearch) {
            if (iconSearch.isBlank()) TaskAppearance.iconNames
            else TaskAppearance.iconNames.filter {
                it.contains(iconSearch.trim(), ignoreCase = true)
            }
        }
        AlertDialog(
            onDismissRequest = { showIconPicker = false; iconSearch = "" },
            title = { Text("Choose Icon") },
            text = {
                Column {
                    // Search field
                    OutlinedTextField(
                        value = iconSearch,
                        onValueChange = { iconSearch = it },
                        placeholder = { Text("Search icons…", style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = {
                            Icon(imageVector = SearchIcon(), contentDescription = "Search", modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (iconSearch.isNotEmpty()) {
                                IconButton(onClick = { iconSearch = "" }, modifier = Modifier.size(20.dp)) {
                                    Icon(imageVector = CloseIcon(), contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    if (filteredIcons.isEmpty()) {
                        Text(
                            "No icons match \"$iconSearch\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp).align(Alignment.CenterHorizontally)
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)
                        ) {
                            items(filteredIcons, key = { it }) { name ->
                                val isSelected = selectedIcon == name
                                val taskColor = TaskAppearance.getColor(selectedColor)
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) taskColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable { selectedIcon = name; showIconPicker = false; iconSearch = "" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = TaskAppearance.getIcon(name),
                                        contentDescription = name,
                                        tint = if (isSelected) taskColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showIconPicker = false; iconSearch = "" }) { Text("Close") } }
        )
    }

    // Past Completions calendar dialog
    if (showPastCompletions && task != null && task.repeatDays != null) {
        PastCompletionsDialog(
            repeatDays = task.repeatDays,
            repeatCount = repeatCount.coerceAtLeast(1),
            taskColor = TaskAppearance.getColor(selectedColor),
            editableHistory = editableHistory,
            onDismiss = { showPastCompletions = false }
        )
    }
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

// ─────────────────────────────────────────────────────────────────────────────
// Past Completions Calendar Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PastCompletionsDialog(
    repeatDays: List<Int>,          // 1=Mon … 7=Sun
    repeatCount: Int,               // max completions per day
    taskColor: Color,
    editableHistory: MutableList<String>,
    onDismiss: () -> Unit
) {
    val tz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date

    // Build an 8-week grid (Mon–Sun columns, week rows), oldest first
    val weeks = 8
    // Find the most-recent Monday ≤ today
    val todayDow = today.dayOfWeek.ordinal          // 0=Mon, 6=Sun
    val mostRecentMonday = today.minus(todayDow, DateTimeUnit.DAY)
    // Grid start = 8 weeks before that Monday
    val gridStart = mostRecentMonday.minus((weeks - 1) * 7, DateTimeUnit.DAY)
    val totalCells = weeks * 7   // 56 cells
    val allDates = List(totalCells) { i -> gridStart.plus(i, DateTimeUnit.DAY) }

    val dow3 = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
    val mon3 = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Past Completions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                // Day-of-week header
                Row(modifier = Modifier.fillMaxWidth()) {
                    dow3.forEach { d ->
                        Text(
                            d,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Calendar grid — 7 cols × 8 rows
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (week in 0 until weeks) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (dow in 0..6) {
                                val date = allDates[week * 7 + dow]
                                val dayIso = dow + 1   // 1=Mon…7=Sun
                                val isScheduled = repeatDays.contains(dayIso)
                                val isFuture = date > today
                                val dateStr = date.toString()
                                val count = editableHistory.count { it == dateStr }

                                if (isScheduled && !isFuture) {
                                    // Tappable cell — cycle count on tap
                                    val bgAlpha = when {
                                        count >= repeatCount -> 1f
                                        count > 0           -> 0.45f + 0.45f * count / repeatCount
                                        else                -> 0f
                                    }
                                    val isComplete = count >= repeatCount
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (bgAlpha > 0f) taskColor.copy(alpha = bgAlpha)
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable {
                                                val newCount = (count + 1) % (repeatCount + 1)
                                                // Remove all entries for this date, then re-add newCount
                                                editableHistory.removeAll { it == dateStr }
                                                repeat(newCount) { editableHistory.add(dateStr) }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(2.dp)
                                        ) {
                                            Text(
                                                "${date.dayOfMonth}",
                                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                                fontWeight = if (isComplete) FontWeight.Bold else FontWeight.Normal,
                                                color = if (bgAlpha > 0.6f) Color.White
                                                        else MaterialTheme.colorScheme.onSurface,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                            if (repeatCount > 1 && count > 0) {
                                                Text(
                                                    "$count/$repeatCount",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                                                    color = if (bgAlpha > 0.6f) Color.White.copy(alpha = 0.85f)
                                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                            } else if (date.dayOfMonth == 1 || (week == 0 && dow == 0)) {
                                                Text(
                                                    mon3[date.monthNumber - 1],
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp),
                                                    color = if (bgAlpha > 0.6f) Color.White.copy(alpha = 0.75f)
                                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Non-scheduled or future day — tiny dim dot
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (isFuture && isScheduled)
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                                else
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                // Month label legend at the bottom
                Spacer(modifier = Modifier.height(6.dp))
                val firstDayOfEachMonth = allDates
                    .filter { it.dayOfMonth == 1 }
                    .distinctBy { it.monthNumber }
                if (firstDayOfEachMonth.isNotEmpty()) {
                    Text(
                        firstDayOfEachMonth.joinToString("  ·  ") { mon3[it.monthNumber - 1] + " ${it.year}" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

