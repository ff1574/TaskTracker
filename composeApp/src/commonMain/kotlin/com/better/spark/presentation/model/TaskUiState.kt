package com.better.spark.presentation.model

import com.better.spark.domain.model.Task

sealed interface TaskUiState {
    data object Loading : TaskUiState
    data class Success(val tasks: List<Task>) : TaskUiState
    data class Error(val message: String) : TaskUiState
}
