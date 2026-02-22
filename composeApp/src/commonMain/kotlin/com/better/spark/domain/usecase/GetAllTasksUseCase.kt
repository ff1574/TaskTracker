package com.better.spark.domain.usecase

import com.better.spark.domain.model.Task
import com.better.spark.domain.repository.TaskRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Use case for observing all tasks.
 * Performs a lazy reset of expired repeatable tasks on startup,
 * and re-checks every 10 seconds so mid-session resets fire promptly.
 */
class GetAllTasksUseCase(
    private val repository: TaskRepository,
    private val clock: Clock
) {
    operator fun invoke(): Flow<List<Task>> {
        return channelFlow {
            // 1. Background periodic reset — fires every 10 s while the screen is alive
            launch {
                while (true) {
                    resetExpiredTasks()
                    delay(10_000)
                }
            }

            // 2. Emit updates from the repository
            repository.observeTasks().collect { send(it) }
        }
    }

    private suspend fun resetExpiredTasks() {
        val now = clock.now()
        val tz = TimeZone.currentSystemDefault()

        repository.getAllTasks().forEach { task ->
            if (!task.isCompleted || task.completedAt == null) return@forEach
            if (task.isArchived) return@forEach  // archived tasks are frozen

            var shouldReset = false

            // Interval-based reset
            if (task.repeatInterval != null) {
                val expirationTime = task.completedAt.plus(task.repeatInterval, DateTimeUnit.MILLISECOND)
                if (now > expirationTime) shouldReset = true
            }

            // Specific-days reset: if today is a scheduled day and it's a different calendar day
            if (!shouldReset && !task.repeatDays.isNullOrEmpty()) {
                val completedDate = task.completedAt.toLocalDateTime(tz).date
                val nowDate = now.toLocalDateTime(tz).date
                if (nowDate > completedDate) {
                    val todayIso = nowDate.dayOfWeek.ordinal + 1 // 1=Mon, 7=Sun
                    if (task.repeatDays.contains(todayIso)) shouldReset = true
                }
            }

            if (shouldReset) {
                // Preserve completionHistory — only clear the active-completion fields
                repository.updateTask(task.copy(isCompleted = false, completedAt = null))
            }
        }
    }
}

