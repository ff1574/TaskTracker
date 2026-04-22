package com.better.spark.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
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
    val repeatCount: Int = 1,
    
    // ── Quitting Bad Habits ──────────────────────────────────────────────────
    /** True if this is a "Quit Bad Habit" task instead of a regular task. */
    val isBadHabit: Boolean = false,
    /** Which type of bad habit this is (Smoking, Alcohol, Screen Time are pre-made). */
    val badHabitType: BadHabitType? = null,
    /** How much time or money is lost every time they relapse. */
    val costPerFailure: Double? = null,
    /** e.g. "$", "min", "h", "€" */
    val costCurrency: String? = null,
    
    // ── Phase 6: Quantifiable Bad Habits ─────────────────────────────────────
    /** The normal daily amount the user consumed before quitting (e.g. 20 cigs/day). */
    val badHabitBaseline: Double? = null,
    /** Time lost per single relapse unit in minutes (e.g. 10 mins per cigarette). */
    val timePerFailure: Double? = null,
    /**
     * Map of Date String (YYYY-MM-DD) -> Number of units consumed that day.
     * This replaces the binary `completionHistory` for bad habits if they log quantities.
     */
    val relapseData: Map<String, Double> = emptyMap()
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

    // ── Quitting Bad Habits Helpers ──────────────────────────────────────────

    /**
     * Calculates how many days clean the user has been for a Bad Habit.
     * Starts counting from the creation date, but resets completely if any relapse Dates exist in completionHistory.
     */
    fun getStreakDays(today: LocalDate): Int {
        if (!isBadHabit) return 0
        
        // Find the most recent relapse if any exists
        val tz = TimeZone.currentSystemDefault()
        
        if (completionHistory.isEmpty()) {
            val creationDate = createdAt.toLocalDateTime(tz).date
            val days = creationDate.daysUntil(today)
            return days.coerceAtLeast(0)
        }

        // Parse history strings into LocalDates and find the max
        val mostRecentRelapseStr = completionHistory.maxOrNull() ?: return 0
        
        return try {
            val maxRelapseDate = LocalDate.parse(mostRecentRelapseStr)
            val days = maxRelapseDate.daysUntil(today)
            days.coerceAtLeast(0)
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Calculates the total amount of units (e.g., cigarettes, drinks, hours) saved since creation.
     */
    fun getTotalSavedUnits(today: LocalDate): Double {
        if (!isBadHabit || badHabitBaseline == null) return 0.0
        
        val tz = TimeZone.currentSystemDefault()
        val creationDate = createdAt.toLocalDateTime(tz).date
        
        // Ensure we don't calculate negative days if today is before creation (timezone edge cases)
        val daysSinceCreation = creationDate.daysUntil(today).coerceAtLeast(0)
        
        // Total they would have consumed
        val baselineTotal = daysSinceCreation * badHabitBaseline
        
        // Total they actually consumed
        val relapsedTotal = relapseData.values.sum()
        
        return (baselineTotal - relapsedTotal).coerceAtLeast(0.0)
    }

    /**
     * Calculates the total money saved.
     */
    fun getTotalMoneySaved(today: LocalDate): Double {
        if (!isBadHabit || costPerFailure == null) return 0.0
        val savedUnits = getTotalSavedUnits(today)
        return savedUnits * costPerFailure
    }
    
    /**
     * Calculates the total time saved in minutes.
     */
    fun getTotalTimeSaved(today: LocalDate): Double {
        if (!isBadHabit || timePerFailure == null) return 0.0
        val savedUnits = getTotalSavedUnits(today)
        return savedUnits * timePerFailure
    }
}

/** Pre-made bad habits physically alter the Life Calendar. */
enum class BadHabitType {
    SMOKING,
    ALCOHOL,
    SCREEN_TIME,
    CUSTOM
}
