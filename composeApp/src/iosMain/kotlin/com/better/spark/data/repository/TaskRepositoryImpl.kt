package com.better.spark.data.repository

import com.better.spark.domain.model.Task
import com.better.spark.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults
import platform.Foundation.removeObjectForKey

/**
 * iOS implementation of TaskRepository using NSUserDefaults.
 * Stores tasks as a JSON string.
 */
class TaskRepositoryImpl : TaskRepository {

    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val json = Json { ignoreUnknownKeys = true }
    private val _tasksFlow = MutableStateFlow<List<Task>>(emptyList())

    init {
        loadTasksFromDefaults()
    }

    private fun loadTasksFromDefaults() {
        val jsonString = userDefaults.stringForKey(KEY_TASKS)
        if (jsonString != null) {
            try {
                val tasks = json.decodeFromString<List<Task>>(jsonString)
                _tasksFlow.value = tasks
            } catch (e: Exception) {
                e.printStackTrace()
                _tasksFlow.value = emptyList()
            }
        }
    }

    private fun saveTasksToDefaults(tasks: List<Task>) {
        val jsonString = json.encodeToString(tasks)
        userDefaults.setObject(jsonString, KEY_TASKS)
    }

    override fun observeTasks(): Flow<List<Task>> = _tasksFlow.asStateFlow()

    override suspend fun getAllTasks(): List<Task> = _tasksFlow.value

    override suspend fun addTask(task: Task) = withContext(Dispatchers.IO) {
        _tasksFlow.update { current ->
            val newList = current + task
            saveTasksToDefaults(newList)
            newList
        }
    }

    override suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        _tasksFlow.update { current ->
            val newList = current.map { if (it.id == task.id) task else it }
            saveTasksToDefaults(newList)
            newList
        }
    }

    override suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        _tasksFlow.update { current ->
            val newList = current.filter { it.id != taskId }
            saveTasksToDefaults(newList)
            newList
        }
    }

    override suspend fun getTaskById(taskId: String): Task? {
        return _tasksFlow.value.find { it.id == taskId }
    }

    override suspend fun clearAll() = withContext(Dispatchers.IO) {
        _tasksFlow.value = emptyList()
        userDefaults.removeObjectForKey(KEY_TASKS)
    }

    companion object {
        private const val KEY_TASKS = "key_tasks_list"
    }
}
