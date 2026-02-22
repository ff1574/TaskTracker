package com.better.spark.domain.usecase

import com.better.spark.domain.repository.TaskRepository

class ArchiveTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: String) {
        val task = repository.getTaskById(taskId) ?: return
        repository.updateTask(task.copy(isArchived = true))
    }
}
