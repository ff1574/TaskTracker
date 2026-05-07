package com.better.spark.presentation.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.better.spark.domain.model.BadHabitType
import com.better.spark.domain.model.Task
import com.better.spark.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    onBack: () -> Unit
) {
    val taskRepo = koinInject<TaskRepository>()
    val clock = koinInject<Clock>()

    var selectedPack by remember { mutableStateOf<TemplatePack?>(null) }
    var importResult by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Templates") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = BackIcon(), contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Choose a pack, preview what it adds, then import it into your local tasks.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            templatePacks.forEach { pack ->
                TemplatePackCard(
                    pack = pack,
                    onPreview = { selectedPack = pack }
                )
            }
        }
    }

    if (selectedPack != null) {
        PreviewPackDialog(
            pack = selectedPack!!,
            onDismiss = { selectedPack = null },
            onImport = {
                val pack = selectedPack!!
                selectedPack = null
                CoroutineScope(Dispatchers.Main).launch {
                    val now = clock.now()
                    pack.items.forEach { item ->
                        taskRepo.addTask(item.toTask(now))
                    }
                    importResult = "Imported “${pack.title}” (${pack.items.size} items)"
                }
            }
        )
    }

    if (importResult != null) {
        AlertDialog(
            onDismissRequest = { importResult = null },
            title = { Text("Imported") },
            text = { Text(importResult!!) },
            confirmButton = { TextButton(onClick = { importResult = null }) { Text("Done") } }
        )
    }
}

@Composable
private fun TemplatePackCard(
    pack: TemplatePack,
    onPreview: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(pack.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(pack.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${pack.items.count { !it.isBadHabit }} tasks · ${pack.items.count { it.isBadHabit }} bad habits",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(onClick = onPreview, shape = RoundedCornerShape(12.dp)) {
                    Text("Preview")
                }
            }
        }
    }
}

@Composable
private fun PreviewPackDialog(
    pack: TemplatePack,
    onDismiss: () -> Unit,
    onImport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(pack.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(pack.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                pack.items.forEach { item ->
                    Text(
                        text = (if (item.isBadHabit) "• Quit: " else "• ") + item.title,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onImport) { Text("Import pack") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private data class TemplatePack(
    val id: String,
    val title: String,
    val description: String,
    val items: List<TemplateItem>
)

private data class TemplateItem(
    val title: String,
    val description: String = "",
    val isBadHabit: Boolean = false,
    val badHabitType: BadHabitType? = null,
    val badHabitBaseline: Double? = null,
    val timePerFailure: Double? = null,
    val costPerFailure: Double? = null,
    val costCurrency: String? = null,
    val repeatDays: List<Int>? = null,
    val iconName: String = "ListChecks",
    val colorHex: String = "#2196F3"
) {
    fun toTask(createdAt: kotlinx.datetime.Instant): Task {
        val id = (createdAt.toEpochMilliseconds() + title.hashCode()).toString()
        return Task(
            id = id,
            title = title,
            description = description,
            createdAt = createdAt,
            repeatDays = repeatDays,
            iconName = iconName,
            colorHex = colorHex,
            isBadHabit = isBadHabit,
            badHabitType = badHabitType,
            badHabitBaseline = badHabitBaseline,
            timePerFailure = timePerFailure,
            costPerFailure = costPerFailure,
            costCurrency = costCurrency
        )
    }
}

private val templatePacks: List<TemplatePack> = listOf(
    TemplatePack(
        id = "starter",
        title = "Starter Routine",
        description = "A simple daily baseline: hydration, movement, and planning.",
        items = listOf(
            TemplateItem(title = "Drink water", description = "1 glass", repeatDays = listOf(1, 2, 3, 4, 5, 6, 7), iconName = "Drop", colorHex = "#42A5F5"),
            TemplateItem(title = "Walk 20 minutes", repeatDays = listOf(1, 2, 3, 4, 5, 6, 7), iconName = "Footprints", colorHex = "#66BB6A"),
            TemplateItem(title = "Plan tomorrow", repeatDays = listOf(1, 2, 3, 4, 5, 6, 7), iconName = "CalendarDots", colorHex = "#AB47BC")
        )
    ),
    TemplatePack(
        id = "focus",
        title = "Focus Pack",
        description = "Small habits that reduce friction and improve consistency.",
        items = listOf(
            TemplateItem(title = "Deep work (30 min)", repeatDays = listOf(1, 2, 3, 4, 5), iconName = "Timer", colorHex = "#FFA726"),
            TemplateItem(title = "Tidy desk", repeatDays = listOf(1, 3, 5), iconName = "Broom", colorHex = "#8D6E63"),
            TemplateItem(title = "No social media before noon", isBadHabit = true, badHabitType = BadHabitType.SCREEN_TIME, badHabitBaseline = 2.0, timePerFailure = 30.0, costPerFailure = 0.0, costCurrency = "$", iconName = "Phone", colorHex = "#EF5350")
        )
    ),
    TemplatePack(
        id = "quit",
        title = "Quit Pack",
        description = "Common quitting goals with savings tracking.",
        items = listOf(
            TemplateItem(title = "Quit nicotine", isBadHabit = true, badHabitType = BadHabitType.SMOKING, badHabitBaseline = 10.0, timePerFailure = 7.0, costPerFailure = 0.5, costCurrency = "$", iconName = "Prohibit", colorHex = "#EF5350"),
            TemplateItem(title = "Quit alcohol", isBadHabit = true, badHabitType = BadHabitType.ALCOHOL, badHabitBaseline = 2.0, timePerFailure = 60.0, costPerFailure = 8.0, costCurrency = "$", iconName = "Wine", colorHex = "#EC407A")
        )
    )
)

