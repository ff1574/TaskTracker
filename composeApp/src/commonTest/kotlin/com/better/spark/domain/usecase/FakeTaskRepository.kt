package com.better.spark.domain.usecase

import com.better.spark.domain.model.Task
import com.better.spark.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake repository implementation for testing.
 * 
 * Best practices:
 * - Simple in-memory implementation for tests
 * - No real database or complex logic needed
 * - Makes tests fast and reliable
 * - Can be reused across multiple test files
 */
class FakeTaskRepository : TaskRepository {
    private val tasks = mutableListOf<Task>()
    private val _tasksFlow = MutableStateFlow<List<Task>>(emptyList())
    
    override fun observeTasks(): Flow<List<Task>> = _tasksFlow.asStateFlow()
    
    override suspend fun getAllTasks(): List<Task> = tasks.toList()
    
    override suspend fun addTask(task: Task) {
        tasks.add(task)
        _tasksFlow.value = tasks.toList()
    }
    
    override suspend fun updateTask(task: Task) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            tasks[index] = task
            _tasksFlow.value = tasks.toList()
        }
    }
    
    override suspend fun deleteTask(taskId: String) {
        tasks.removeIf { it.id == taskId }
        _tasksFlow.value = tasks.toList()
    }
    
    override suspend fun getTaskById(taskId: String): Task? {
        return tasks.firstOrNull { it.id == taskId }
    }

    override suspend fun clearAll() {
        tasks.clear()
        _tasksFlow.value = emptyList()
    }
}
