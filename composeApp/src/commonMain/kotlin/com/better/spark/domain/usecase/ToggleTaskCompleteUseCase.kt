package com.better.spark.domain.usecase

import com.better.spark.domain.repository.TaskRepository
import kotlinx.datetime.Clock

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Use case to toggle the completion status of a task or log a relapse amount for bad habits.
 * 
 * Business Logic:
 * - If incomplete -> mark complete and set completedAt to now.
 * - If complete -> mark incomplete and clear completedAt.
 * - For bad habits, if [relapseAmount] is provided, it records the exact amount consumed today 
 *   into the `relapseData` map and adds today to `completionHistory`.
 */
class ToggleTaskCompleteUseCase(
    private val taskRepository: TaskRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(taskId: String, relapseAmount: Double? = null) {
        val task = taskRepository.getTaskById(taskId) ?: return
        
        val updatedTask = if (task.isBadHabit && relapseAmount != null) {
            val tz = TimeZone.currentSystemDefault()
            val todayStr = clock.now().toLocalDateTime(tz).date.toString()
            
            // Add or accumulate the relapse amount for today
            val currentAmount = task.relapseData[todayStr] ?: 0.0
            val newRelapseData = task.relapseData.toMutableMap().apply {
                put(todayStr, currentAmount + relapseAmount)
            }
            
            // Also add to completionHistory so that the basic streak logic still resets
            val newHistory = if (!task.completionHistory.contains(todayStr)) {
                task.completionHistory + todayStr
            } else {
                task.completionHistory
            }
            
            task.copy(
                relapseData = newRelapseData,
                completionHistory = newHistory
            )
        } else {
            task.toggleComplete(clock.now())
        }
        
        taskRepository.updateTask(updatedTask)
    }
}
