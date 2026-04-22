package com.better.spark.domain.usecase

import com.better.spark.domain.model.Task
import com.better.spark.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Returns only the tasks that are marked as bad habits.
 * Note: Reset logic and expiration is mostly handled by GetAllTasksUseCase, 
 * but bad habits don't expire in the same way (they just track raw sobriety streaks).
 */
class GetBadHabitsUseCase(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        return repository.observeTasks().map { tasks ->
            tasks.filter { it.isBadHabit }
        }
    }
}
