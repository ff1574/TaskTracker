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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.better.spark.domain.model.RelapseJournalEntry
import com.better.spark.presentation.viewmodel.RelapseJournalViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelapseJournalScreen(
    badHabitId: String,
    onBack: () -> Unit
) {
    val viewModel = koinViewModel<RelapseJournalViewModel>()
    LaunchedEffect(badHabitId) { viewModel.setHabitId(badHabitId) }

    val entries by viewModel.entriesForCurrentHabit.collectAsState()
    var editing by remember { mutableStateOf<RelapseJournalEntry?>(null) }
    var confirmDelete by remember { mutableStateOf<RelapseJournalEntry?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Relapse Journal") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (entries.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("No journal entries yet.", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Log a relapse from the Quitting screen.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                entries.forEach { entry ->
                    RelapseJournalEntryCard(
                        entry = entry,
                        onEdit = { editing = entry },
                        onDelete = { confirmDelete = entry }
                    )
                }
            }
        }
    }

    if (editing != null) {
        EditRelapseEntryDialog(
            entry = editing!!,
            onDismiss = { editing = null },
            onSave = { updated ->
                viewModel.updateEntry(updated)
                editing = null
            }
        )
    }

    if (confirmDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Delete entry?") },
            text = { Text("This can’t be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEntry(confirmDelete!!.id)
                        confirmDelete = null
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun RelapseJournalEntryCard(
    entry: RelapseJournalEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val local = entry.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    val dateText = "%04d-%02d-%02d %02d:%02d".format(
        local.year, local.monthNumber, local.dayOfMonth, local.hour, local.minute
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Amount: ${entry.amount}", fontWeight = FontWeight.Bold)
                    Text(dateText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = onEdit) { Text("Edit") }
                    OutlinedButton(onClick = onDelete) { Text("Delete") }
                }
            }

            if (!entry.mood.isNullOrBlank()) {
                Text("Mood: ${entry.mood}", style = MaterialTheme.typography.bodySmall)
            }
            if (entry.triggers.isNotEmpty()) {
                Text("Triggers: ${entry.triggers.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
            }
            if (!entry.notes.isNullOrBlank()) {
                Text(entry.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EditRelapseEntryDialog(
    entry: RelapseJournalEntry,
    onDismiss: () -> Unit,
    onSave: (RelapseJournalEntry) -> Unit
) {
    var amountStr by remember { mutableStateOf(entry.amount.toString()) }
    var mood by remember { mutableStateOf(entry.mood ?: "") }
    var triggersCsv by remember { mutableStateOf(entry.triggers.joinToString(", ")) }
    var notes by remember { mutableStateOf(entry.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amountStr = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = mood,
                    onValueChange = { mood = it },
                    label = { Text("Mood") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = triggersCsv,
                    onValueChange = { triggersCsv = it },
                    label = { Text("Triggers (comma-separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amt = amountStr.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        val updated = entry.copy(
                            amount = amt,
                            mood = mood.trim().ifBlank { null },
                            triggers = triggersCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }.distinct(),
                            notes = notes.trim().ifBlank { null }
                        )
                        onSave(updated)
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

