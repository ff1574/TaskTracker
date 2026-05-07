package com.better.spark.domain.repository

import com.better.spark.domain.model.RelapseJournalEntry
import kotlinx.coroutines.flow.Flow

interface RelapseJournalRepository {
    fun observeEntries(): Flow<List<RelapseJournalEntry>>
    suspend fun getAllEntries(): List<RelapseJournalEntry>
    suspend fun addEntry(entry: RelapseJournalEntry)
    suspend fun updateEntry(entry: RelapseJournalEntry)
    suspend fun deleteEntry(entryId: String)
    suspend fun getEntryById(entryId: String): RelapseJournalEntry?
}

