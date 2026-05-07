package com.better.spark.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.better.spark.domain.model.BadHabitType
import com.better.spark.domain.model.Task

@Composable
fun BadHabitDialog(
    task: Task? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        iconName: String,
        colorHex: String,
        type: BadHabitType,
        badHabitBaseline: Double?,
        timePerFailure: Double?,
        costPerFailure: Double?,
        costCurrency: String?
    ) -> Unit,
    onDelete: (() -> Unit)? = null,
    onArchive: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }

    var selectedIcon by remember { mutableStateOf(task?.iconName ?: "Prohibit") }
    var selectedColor by remember { mutableStateOf(task?.colorHex ?: "#F44336") }

    var showColorPicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    var selectedType by remember { mutableStateOf(task?.badHabitType ?: BadHabitType.CUSTOM) }
    
    // New Quantity state
    var baselineValue by remember { mutableStateOf(task?.badHabitBaseline?.toString() ?: "") }
    var timeLostValue by remember { mutableStateOf(task?.timePerFailure?.toString() ?: "") }
    var costValue by remember { mutableStateOf(task?.costPerFailure?.toString() ?: "") }
    var currency by remember { mutableStateOf(task?.costCurrency ?: "$") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showArchiveConfirm by remember { mutableStateOf(false) }
    
    // Auto-fill currency initially depending on region or default
    LaunchedEffect(selectedType) {
        if (task == null && currency.isBlank()) currency = "$"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = BackIcon(), contentDescription = "Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (task == null) "New Bad Habit" else "Edit Bad Habit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // Habit Type Selection
                Text("Select Category:", style = MaterialTheme.typography.labelLarge)
                val chipScroll = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(chipScroll),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BadHabitType.entries.forEach { type ->
                        val label = when (type) {
                            BadHabitType.SMOKING -> "Smoking"
                            BadHabitType.ALCOHOL -> "Alcohol"
                            BadHabitType.SCREEN_TIME -> "Screen time"
                            BadHabitType.CUSTOM -> "Custom"
                        }
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(label, maxLines = 1) }
                        )
                    }
                }

                // Title & Description
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details or Motivation (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
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
                            Icon(imageVector = TaskAppearance.getIcon(selectedIcon), contentDescription = "Icon", tint = TaskAppearance.getColor(selectedColor))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Icon")
                        }
                    }
                }

                // Baseline Tracking
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                when (selectedType) {
                    BadHabitType.SMOKING -> {
                        Text("Add Specifics (Optional but Recommended):", style = MaterialTheme.typography.labelLarge)
                        OutlinedTextField(
                            value = baselineValue,
                            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) baselineValue = it },
                            label = { Text("Average Cigarettes Smoked Per Day") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = costValue,
                                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) costValue = it },
                                label = { Text("Price per Pack (20 cigs)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = currency,
                                onValueChange = { currency = it },
                                label = { Text("Currency") },
                                modifier = Modifier.weight(0.5f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    BadHabitType.ALCOHOL -> {
                        Text("Add Specifics (Optional but Recommended):", style = MaterialTheme.typography.labelLarge)
                        OutlinedTextField(
                            value = baselineValue,
                            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) baselineValue = it },
                            label = { Text("Average Drinks Per Day") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = costValue,
                                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) costValue = it },
                                label = { Text("Avg Price per Drink") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = currency,
                                onValueChange = { currency = it },
                                label = { Text("Currency") },
                                modifier = Modifier.weight(0.5f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    else -> {
                        Text("Add Specifics (Optional but Recommended):", style = MaterialTheme.typography.labelLarge)
                        OutlinedTextField(
                            value = baselineValue,
                            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) baselineValue = it },
                            label = { Text("Baseline: Quantity/frequency") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = timeLostValue,
                                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) timeLostValue = it },
                                label = { Text("Time lost (minutes)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = costValue,
                                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) costValue = it },
                                label = { Text("Money lost", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = currency,
                                onValueChange = { currency = it },
                                label = { Text("Unit", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            // Full-width icon bar: Delete | Archive | Confirm
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    enabled = task != null && onDelete != null
                ) {
                    Icon(
                        imageVector = DeleteIcon(),
                        contentDescription = "Delete Habit",
                        tint = if (task != null && onDelete != null)
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
                // Archive
                IconButton(
                    onClick = { showArchiveConfirm = true },
                    enabled = task != null && onArchive != null
                ) {
                    Icon(
                        imageVector = ArchiveIcon(),
                        contentDescription = "Archive Habit",
                        tint = if (task != null && onArchive != null)
                            MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
                // Confirm
                IconButton(
                    onClick = {
                        if (title.isNotBlank()) {
                            val costRaw = costValue.toDoubleOrNull()
                            
                            // For smoking, costValue is per Pack (20). To get costPerFailure (1 cigarette):
                            val finalCostPerFail = if (selectedType == BadHabitType.SMOKING && costRaw != null) {
                                costRaw / 20.0
                            } else {
                                costRaw
                            }
                            
                            // For smoking missing timeLostValue default approx 10 mins per cigarette
                            val finalTimePerFail = if (selectedType == BadHabitType.SMOKING && timeLostValue.isBlank()) {
                                10.0
                            } else {
                                timeLostValue.toDoubleOrNull()
                            }
                            
                            val curr = currency.takeIf { it.isNotBlank() }
                            val baseline = baselineValue.toDoubleOrNull()
                            
                            onConfirm(
                                title.trim(), 
                                description.trim(), 
                                selectedIcon, 
                                selectedColor, 
                                selectedType, 
                                baseline,
                                finalTimePerFail,
                                finalCostPerFail, 
                                curr
                            )
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

    if (showDeleteConfirm && task != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete habit?") },
            text = { Text("“${task.title}” will be removed permanently.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }

    if (showArchiveConfirm && task != null && onArchive != null) {
        AlertDialog(
            onDismissRequest = { showArchiveConfirm = false },
            title = { Text("Archive habit?") },
            text = { Text("“${task.title}” will move to Archive and can be restored later.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showArchiveConfirm = false
                        onArchive()
                    }
                ) { Text("Archive") }
            },
            dismissButton = { TextButton(onClick = { showArchiveConfirm = false }) { Text("Cancel") } }
        )
    }

    // Reusing the same color/icon picker modals...
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
}
