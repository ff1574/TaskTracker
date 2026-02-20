package com.better.spark.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Task domain model representing a single task.
 * 
 * Following best practices:
 * - Immutable data class (all properties are val)
 * - Uses kotlinx.datetime for proper date/time handling
 * - Serializable for easy persistence
 * - Clear, self-documenting property names
 */
@Serializable
data class Task(
    val id: String,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Instant,
    val completedAt: Instant? = null,
    val repeatInterval: Long? = null, // Interval in milliseconds
    val repeatDays: List<Int>? = null // 1 = Monday, 7 = Sunday (ISO-8601)
) {
    /**
     * Creates a copy of this task with the completion status toggled.
     * Follows immutability principle - returns new instance rather than mutating.
     */
    fun toggleComplete(timestamp: Instant): Task {
        return copy(
            isCompleted = !isCompleted,
            completedAt = if (!isCompleted) timestamp else null
        )
    }
}
