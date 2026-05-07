package com.better.spark.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class RelapseJournalEntry(
    val id: String,
    val badHabitId: String,
    val amount: Double,
    val timestamp: Instant,
    val mood: String? = null,
    val triggers: List<String> = emptyList(),
    val notes: String? = null
)

