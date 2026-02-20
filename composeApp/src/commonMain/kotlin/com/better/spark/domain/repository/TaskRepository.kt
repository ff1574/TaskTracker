package com.better.spark.domain.repository

import com.better.spark.domain.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Task data operations.
 * 
 * Following Clean Architecture principles:
 * - Interface in domain layer (dependency inversion principle)
 * - Implementation will be in data layer
 * - Uses Flow for reactive data streams
 * - Platform-agnostic (no Android/iOS specific types)
 */
interface TaskRepository {
    /**
     * Observes all tasks. Returns a Flow that emits updates whenever tasks change.
     * Using Flow instead of LiveData for multiplatform compatibility.
     */
    fun observeTasks(): Flow<List<Task>>
    
    /**
     * Gets all tasks as a single snapshot.
     * Suspend function for asynchronous execution without blocking.
     */
    suspend fun getAllTasks(): List<Task>
    
    /**
     * Adds a new task.
     */
    suspend fun addTask(task: Task)
    
    /**
     * Updates an existing task.
     */
    suspend fun updateTask(task: Task)
    
    /**
     * Deletes a task by ID.
     */
    suspend fun deleteTask(taskId: String)
    
    /**
     * Gets a single task by ID.
     */
    suspend fun getTaskById(taskId: String): Task?
}
