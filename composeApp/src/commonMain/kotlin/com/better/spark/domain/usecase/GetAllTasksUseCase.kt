package com.better.spark.domain.usecase

import com.better.spark.domain.model.Task
import com.better.spark.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.plus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Use case for observing all tasks.
 * 
 * Following Single Responsibility Principle:
 * - Each use case encapsulates a single business operation
 * - Depends only on repository abstraction (dependency inversion)
 * 
 * Using operator invoke pattern for clean call syntax:
 * - Can call as: getAllTasksUseCase() instead of getAllTasksUseCase.execute()
 */
class GetAllTasksUseCase(
    private val repository: TaskRepository,
    private val clock: Clock
) {
    /**
     * Returns a Flow of tasks.
     * Also performs a "lazy reset" of repeatable tasks that have expired.
     */
    operator fun invoke(): Flow<List<Task>> {
        // We perform the check asynchronously/side-effect when the flow is collected
        // or we could do it here if we had a scope. 
        // Since we are inside a simple function, we can return a flow that checks on start.
        
        return channelFlow {
            // 1. Initial check for expired tasks
            val tasks = repository.getAllTasks()
            val now = clock.now()
            
            tasks.forEach { task ->
                if (task.isCompleted && task.completedAt != null) {
                    var shouldReset = false
                    
                    // 1. Interval Reset
                    if (task.repeatInterval != null) {
                        val expirationTime = task.completedAt.plus(task.repeatInterval, kotlinx.datetime.DateTimeUnit.MILLISECOND)
                        if (now > expirationTime) shouldReset = true
                    }
                    
                    // 2. Specific Days Reset
                    // Logic: If today is a "scheduled day" AND it matches or is after the next scheduled instance since completion
                    if (!shouldReset && task.repeatDays != null && task.repeatDays.isNotEmpty()) {
                        val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
                        val completedDateTime = task.completedAt.toLocalDateTime(tz)
                        val completedDate = completedDateTime.date
                        
                        val nowDate = now.toLocalDateTime(tz).date
                        
                        // Only reset if we are on a different day than completion
                        if (nowDate > completedDate) {
                             // Check if today is one of the repeat days
                             // isoDayNumber: 1=Monday, 7=Sunday
                             val todayIso = nowDate.dayOfWeek.ordinal + 1
                             if (task.repeatDays.contains(todayIso)) {
                                 shouldReset = true
                             }
                        }
                    }

                    if (shouldReset) {
                        // Reset the task
                        repository.updateTask(task.copy(isCompleted = false, completedAt = null))
                    }
                }
            }
            
            // 2. Emit updates from the repository
            repository.observeTasks().collect { send(it) }
        }
    }
}
