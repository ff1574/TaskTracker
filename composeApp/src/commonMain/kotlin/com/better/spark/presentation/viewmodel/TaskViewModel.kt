package com.better.spark.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.better.spark.domain.model.Task
import com.better.spark.domain.usecase.AddTaskUseCase
import com.better.spark.domain.usecase.ArchiveTaskUseCase
import com.better.spark.domain.usecase.GetAllTasksUseCase
import com.better.spark.domain.usecase.ToggleTaskCompleteUseCase
import com.better.spark.domain.usecase.UpdateTaskUseCase
import com.better.spark.domain.usecase.DeleteTaskUseCase
import com.better.spark.domain.usecase.UnarchiveTaskUseCase
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
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val archiveTaskUseCase: ArchiveTaskUseCase,
    private val unarchiveTaskUseCase: UnarchiveTaskUseCase
) : ViewModel() {

    /** All non-archived tasks (active list). */
    val uiState: StateFlow<TaskUiState> = getAllTasksUseCase()
        .map { tasks -> TaskUiState.Success(tasks.filter { !it.isArchived }) as TaskUiState }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TaskUiState.Loading
        )

    /** Archived tasks (for the archive sheet). */
    val archivedUiState: StateFlow<List<Task>> = getAllTasksUseCase()
        .map { tasks -> tasks.filter { it.isArchived } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun generateId(): String {
        return Clock.System.now().toEpochMilliseconds().toString()
    }

    fun addTask(
        title: String,
        description: String = "",
        repeatInterval: Long? = null,
        repeatDays: List<Int>? = null,
        iconName: String = "Default",
        colorHex: String = "#BB86FC",
        repeatCount: Int = 1
    ) {
        viewModelScope.launch {
            val task = Task(
                id = generateId(),
                title = title,
                description = description,
                isCompleted = false,
                createdAt = Clock.System.now(),
                repeatInterval = repeatInterval,
                repeatDays = repeatDays,
                iconName = iconName,
                colorHex = colorHex,
                repeatCount = repeatCount
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

    fun archiveTask(taskId: String) {
        viewModelScope.launch {
            archiveTaskUseCase(taskId)
        }
    }

    fun unarchiveTask(taskId: String) {
        viewModelScope.launch {
            unarchiveTaskUseCase(taskId)
        }
    }
}
