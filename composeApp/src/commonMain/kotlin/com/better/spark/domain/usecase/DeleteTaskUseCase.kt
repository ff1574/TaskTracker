package com.better.spark.domain.usecase

import com.better.spark.domain.repository.TaskRepository

class DeleteTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: String) {
        repository.deleteTask(taskId)
    }
}
