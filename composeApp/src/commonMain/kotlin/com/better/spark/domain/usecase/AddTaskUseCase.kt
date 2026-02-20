package com.better.spark.domain.usecase

import com.better.spark.domain.model.Task
import com.better.spark.domain.repository.TaskRepository

/**
 * Use case for adding a new task.
 * 
 * Encapsulates the business logic for task creation.
 * Can be extended in the future to add validation, notifications, etc.
 */
class AddTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        // Future: Add validation logic here
        // Future: Trigger analytics/notifications
        repository.addTask(task)
    }
}
