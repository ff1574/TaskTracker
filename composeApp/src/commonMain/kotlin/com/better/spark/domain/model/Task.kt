package com.better.spark.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Instant,
    val completedAt: Instant? = null,
    val repeatInterval: Long? = null, // Interval in milliseconds
    val repeatDays: List<Int>? = null, // 1 = Monday, 7 = Sunday (ISO-8601)
    val iconName: String = "Default",
    val colorHex: String = "#BB86FC",
    /**
     * For specific-day tasks: stores YYYY-MM-DD strings; same date may appear
     * multiple times when repeatCount > 1 (one entry per completion tap).
     * For interval tasks: stores epoch-millis strings (one per window completion).
     */
    val completionHistory: List<String> = emptyList(),
    /** When true, the task is hidden from the main list and reset logic is paused. */
    val isArchived: Boolean = false,
    /**
     * How many completions are required per scheduled day (specific-day tasks only).
     * Defaults to 1. Has no effect on interval tasks.
     */
    val repeatCount: Int = 1
) {
    val isRepeatable: Boolean
        get() = repeatInterval != null || (!repeatDays.isNullOrEmpty())

    /** Count of how many times today has been completed (specific-day tasks). */
    fun todayCompletionCount(timestamp: Instant): Int {
        if (repeatInterval != null) return if (isCompleted) 1 else 0
        val today = timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        return completionHistory.count { it == today }
    }

    /**
     * Adds or removes one completion entry.
     *
     * - **Interval tasks**: binary toggle stored as epoch-millis.
     * - **Specific-day tasks**: each tap adds one YYYY-MM-DD entry (up to repeatCount);
     *   when fully completed, the next tap removes one entry (un-completing).
     */
    fun toggleComplete(timestamp: Instant): Task {
        val tz = TimeZone.currentSystemDefault()

        return if (isRepeatable) {
            if (repeatInterval != null) {
                // ── Interval task ──────────────────────────────────────────
                val newHistory = if (isCompleted) {
                    if (completionHistory.isEmpty()) completionHistory
                    else completionHistory.dropLast(1)
                } else {
                    (completionHistory + timestamp.toEpochMilliseconds().toString()).distinct()
                }
                copy(
                    isCompleted = !isCompleted,
                    completedAt = if (!isCompleted) timestamp else null,
                    completionHistory = newHistory
                )
            } else {
                // ── Specific-day task ───────────────────────────────────────
                val today = timestamp.toLocalDateTime(tz).date.toString()
                val todayCount = completionHistory.count { it == today }
                val rc = repeatCount.coerceAtLeast(1)

                if (isCompleted) {
                    // Un-complete: remove one today entry
                    val idx = completionHistory.indexOfLast { it == today }
                    val newHistory = if (idx >= 0) {
                        completionHistory.toMutableList().also { it.removeAt(idx) }
                    } else completionHistory
                    copy(isCompleted = false, completedAt = null, completionHistory = newHistory)
                } else {
                    // Add one more completion for today
                    val newCount = todayCount + 1
                    val newHistory = completionHistory + today
                    val nowComplete = newCount >= rc
                    copy(
                        isCompleted = nowComplete,
                        completedAt = if (nowComplete) timestamp else null,
                        completionHistory = newHistory
                    )
                }
            }
        } else {
            copy(
                isCompleted = !isCompleted,
                completedAt = if (!isCompleted) timestamp else null
            )
        }
    }
}
