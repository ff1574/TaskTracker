package com.better.spark.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.better.spark.domain.model.Task
import com.better.spark.domain.usecase.AddTaskUseCase
import com.better.spark.domain.usecase.ArchiveTaskUseCase
import com.better.spark.domain.usecase.DeleteTaskUseCase
import com.better.spark.domain.usecase.GetBadHabitsUseCase
import com.better.spark.domain.usecase.ToggleTaskCompleteUseCase
import com.better.spark.domain.usecase.UnarchiveTaskUseCase
import com.better.spark.domain.usecase.UpdateTaskUseCase
import com.better.spark.presentation.model.TaskUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class BadHabitViewModel(
    private val getBadHabitsUseCase: GetBadHabitsUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val toggleTaskCompleteUseCase: ToggleTaskCompleteUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val archiveTaskUseCase: ArchiveTaskUseCase,
    private val unarchiveTaskUseCase: UnarchiveTaskUseCase
) : ViewModel() {

    /** All non-archived BAD HABITS (active list). */
    val uiState: StateFlow<TaskUiState> = getBadHabitsUseCase()
        .map { tasks -> TaskUiState.Success(tasks.filter { !it.isArchived }) as TaskUiState }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TaskUiState.Loading
        )

    /** Archived bad habits (for the archive sheet). */
    val archivedHabits: StateFlow<List<Task>> = getBadHabitsUseCase()
        .map { tasks -> tasks.filter { it.isArchived } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addBadHabit(task: Task) {
        viewModelScope.launch {
            addTaskUseCase(task)
        }
    }

    fun updateBadHabit(task: Task) {
        viewModelScope.launch {
            updateTaskUseCase(task)
        }
    }

    fun deleteBadHabit(id: String) {
        viewModelScope.launch {
            deleteTaskUseCase(id)
        }
    }

    fun toggleRelapse(id: String, amount: Double? = null) {
        viewModelScope.launch {
            toggleTaskCompleteUseCase(id, amount)
        }
    }
    
    fun archiveBadHabit(id: String) {
        viewModelScope.launch {
            archiveTaskUseCase(id)
        }
    }
    
    fun unarchiveBadHabit(id: String) {
        viewModelScope.launch {
            unarchiveTaskUseCase(id)
        }
    }
}
