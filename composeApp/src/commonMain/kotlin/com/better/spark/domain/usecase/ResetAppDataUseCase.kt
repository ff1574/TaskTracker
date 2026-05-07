package com.better.spark.domain.usecase

import com.better.spark.domain.repository.RelapseJournalRepository
import com.better.spark.domain.repository.SettingsRepository
import com.better.spark.domain.repository.TaskRepository

class ResetAppDataUseCase(
    private val taskRepository: TaskRepository,
    private val settingsRepository: SettingsRepository,
    private val relapseJournalRepository: RelapseJournalRepository
) {
    suspend operator fun invoke() {
        // Order doesn't matter much, but clear settings last so the UI will
        // immediately fall back to onboarding after reset.
        relapseJournalRepository.clearAll()
        taskRepository.clearAll()
        settingsRepository.clearAll()
    }
}

