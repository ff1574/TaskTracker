package com.better.spark.domain.usecase

import com.better.spark.domain.repository.TaskRepository
import kotlinx.datetime.Clock

/**
 * Use case to toggle the completion status of a task.
 * 
 * Business Logic:
 * - If incomplete -> mark complete and set completedAt to now.
 * - If complete -> mark incomplete and clear completedAt.
 */
class ToggleTaskCompleteUseCase(
    private val taskRepository: TaskRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(taskId: String) {
        val task = taskRepository.getTaskById(taskId) ?: return
        val updatedTask = task.toggleComplete(clock.now())
        taskRepository.updateTask(updatedTask)
    }
}
