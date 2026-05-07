package com.better.spark.data.repository

import android.content.Context
import androidx.core.content.edit
import com.better.spark.domain.model.Task
import com.better.spark.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Android implementation of TaskRepository using SharedPreferences.
 * Stores tasks as a JSON string.
 */
class TaskRepositoryImpl(
    private val context: Context
) : TaskRepository {

    private val prefs by lazy {
        context.getSharedPreferences("task_data", Context.MODE_PRIVATE)
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val _tasksFlow = MutableStateFlow<List<Task>>(emptyList())

    init {
        // Load initial data synchronously or launch a coroutine?
        // For simplicity in this architecture, we'll load on init but ideally suspend.
        // Since constructor can't suspend, we'll use a crude blocking load or launch scope if we had one.
        // Better: just read from prefs immediately since it's "remember in phone memory"
        loadTasksFromPrefs()
    }

    private fun loadTasksFromPrefs() {
        val jsonString = prefs.getString(KEY_TASKS, null)
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

    private fun saveTasksToPrefs(tasks: List<Task>) {
        val jsonString = json.encodeToString(tasks)
        prefs.edit { putString(KEY_TASKS, jsonString) }
    }

    override fun observeTasks(): Flow<List<Task>> = _tasksFlow.asStateFlow()

    override suspend fun getAllTasks(): List<Task> = _tasksFlow.value

    override suspend fun addTask(task: Task) = withContext(Dispatchers.IO) {
        _tasksFlow.update { current ->
            val newList = current + task
            saveTasksToPrefs(newList)
            newList
        }
    }

    override suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        _tasksFlow.update { current ->
            val newList = current.map { if (it.id == task.id) task else it }
            saveTasksToPrefs(newList)
            newList
        }
    }

    override suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        _tasksFlow.update { current ->
            val newList = current.filter { it.id != taskId }
            saveTasksToPrefs(newList)
            newList
        }
    }

    override suspend fun getTaskById(taskId: String): Task? {
        return _tasksFlow.value.find { it.id == taskId }
    }

    override suspend fun clearAll() = withContext(Dispatchers.IO) {
        _tasksFlow.value = emptyList()
        prefs.edit { remove(KEY_TASKS) }
    }

    companion object {
        private const val KEY_TASKS = "key_tasks_list"
    }
}
