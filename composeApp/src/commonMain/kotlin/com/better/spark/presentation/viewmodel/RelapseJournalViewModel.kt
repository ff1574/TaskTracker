package com.better.spark.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.better.spark.domain.model.RelapseJournalEntry
import com.better.spark.domain.repository.RelapseJournalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class RelapseJournalViewModel(
    private val repo: RelapseJournalRepository,
    private val clock: Clock
) : ViewModel() {

    private val _currentHabitId = MutableStateFlow<String?>(null)
    val currentHabitId: StateFlow<String?> = _currentHabitId.asStateFlow()

    fun setHabitId(habitId: String) {
        _currentHabitId.value = habitId
    }

    val entriesForCurrentHabit: StateFlow<List<RelapseJournalEntry>> =
        combine(repo.observeEntries(), _currentHabitId) { entries, habitId ->
            if (habitId.isNullOrBlank()) emptyList()
            else entries.filter { it.badHabitId == habitId }
                .sortedByDescending { it.timestamp }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEntry(
        badHabitId: String,
        amount: Double,
        timestampMillis: Long,
        mood: String?,
        triggers: List<String>,
        notes: String?
    ) {
        viewModelScope.launch {
            val entry = RelapseJournalEntry(
                id = clock.now().toEpochMilliseconds().toString(),
                badHabitId = badHabitId,
                amount = amount,
                timestamp = kotlinx.datetime.Instant.fromEpochMilliseconds(timestampMillis),
                mood = mood?.takeIf { it.isNotBlank() },
                triggers = triggers.map { it.trim() }.filter { it.isNotBlank() }.distinct(),
                notes = notes?.takeIf { it.isNotBlank() }
            )
            repo.addEntry(entry)
        }
    }

    fun updateEntry(entry: RelapseJournalEntry) {
        viewModelScope.launch {
            repo.updateEntry(entry)
        }
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            repo.deleteEntry(entryId)
        }
    }
}

