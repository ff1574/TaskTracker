package com.better.spark.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.better.spark.domain.model.Task
import com.better.spark.domain.usecase.AddTaskUseCase
import com.better.spark.domain.usecase.GetAllTasksUseCase
import com.better.spark.domain.usecase.ToggleTaskCompleteUseCase
import com.better.spark.domain.usecase.UpdateTaskUseCase
import com.better.spark.domain.usecase.DeleteTaskUseCase
import com.better.spark.presentation.model.TaskUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class TaskViewModel(
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val toggleTaskCompleteUseCase: ToggleTaskCompleteUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    val uiState: StateFlow<TaskUiState> = getAllTasksUseCase()
        .map { tasks -> TaskUiState.Success(tasks) as TaskUiState }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TaskUiState.Loading
        )

    private fun generateId(): String {
        return Clock.System.now().toEpochMilliseconds().toString()
    }

    fun addTask(title: String, description: String = "", repeatInterval: Long? = null, repeatDays: List<Int>? = null) {
        viewModelScope.launch {
            val task = Task(
                id = generateId(),
                title = title,
                description = description,
                isCompleted = false,
                createdAt = Clock.System.now(),
                repeatInterval = repeatInterval,
                repeatDays = repeatDays
            )
            addTaskUseCase(task)
        }
    }

    fun toggleTaskComplete(taskId: String) {
        viewModelScope.launch {
            toggleTaskCompleteUseCase(taskId)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            updateTaskUseCase(task)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
        }
    }
}
