package com.better.spark.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.better.spark.domain.model.BadHabitType
import com.better.spark.domain.model.Task
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun RelapseAmountDialog(
    task: Task,
    onDismiss: () -> Unit,
    onConfirm: (
        amount: Double,
        notes: String?,
        triggers: List<String>,
        mood: String?,
        timestampMillis: Long
    ) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var triggersCsv by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("") }

    val nowMillis = remember { Clock.System.now().toEpochMilliseconds() }
    var selectedTimestampMillis by remember { mutableStateOf(nowMillis) }
    var showDatePicker by remember { mutableStateOf(false) }
    var timeStr by remember {
        val local = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        mutableStateOf("%02d:%02d".format(local.hour, local.minute))
    }
    
    val unitLabel = when (task.badHabitType) {
        BadHabitType.SMOKING -> "Cigarettes"
        BadHabitType.ALCOHOL -> "Drinks"
        else -> "Amount"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Log Relapse",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "How many $unitLabel did you have today?",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { 
                        if (it.isEmpty() || it.toDoubleOrNull() != null) {
                            amountStr = it 
                        }
                    },
                    label = { Text(unitLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Optional details
                OutlinedTextField(
                    value = mood,
                    onValueChange = { mood = it },
                    label = { Text("Mood (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )

                OutlinedTextField(
                    value = triggersCsv,
                    onValueChange = { triggersCsv = it },
                    label = { Text("Triggers (comma-separated, optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Pick date")
                    }
                    OutlinedTextField(
                        value = timeStr,
                        onValueChange = { timeStr = it },
                        label = { Text("Time (HH:MM)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        val triggers = triggersCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        val moodVal = mood.trim().ifBlank { null }
                        val notesVal = notes.trim().ifBlank { null }

                        val finalTimestampMillis = combineDateAndTimeMillis(
                            dateMillis = selectedTimestampMillis,
                            timeStr = timeStr
                        )

                        onConfirm(amount, notesVal, triggers, moodVal, finalTimestampMillis)
                    }
                },
                enabled = amountStr.toDoubleOrNull()?.let { it > 0 } == true
            ) {
                Text("Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(28.dp)
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedTimestampMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedTimestampMillis = it }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun combineDateAndTimeMillis(dateMillis: Long, timeStr: String): Long {
    // dateMillis from DatePicker is already anchored at midnight UTC-ish; we keep it simple:
    // parse "HH:MM" and add minutes offset.
    val parts = timeStr.trim().split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val hh = h.coerceIn(0, 23)
    val mm = m.coerceIn(0, 59)
    return dateMillis + (hh * 60L + mm) * 60_000L
}
