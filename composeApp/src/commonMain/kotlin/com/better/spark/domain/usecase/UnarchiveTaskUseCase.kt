package com.better.spark.domain.usecase

import com.better.spark.domain.repository.TaskRepository

class UnarchiveTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: String) {
        val task = repository.getTaskById(taskId) ?: return
        val restored = if (task.isRepeatable) {
            task.copy(isArchived = false)
        } else {
            // Reset completion so the auto-archive trigger doesn't fire again
            task.copy(isArchived = false, isCompleted = false, completedAt = null)
        }
        repository.updateTask(restored)
    }
}
