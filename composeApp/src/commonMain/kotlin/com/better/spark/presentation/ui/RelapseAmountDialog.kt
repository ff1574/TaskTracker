package com.better.spark.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.better.spark.domain.model.BadHabitType
import com.better.spark.domain.model.Task

@Composable
fun RelapseAmountDialog(
    task: Task,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onConfirm(amount)
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
}
