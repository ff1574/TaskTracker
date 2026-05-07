package com.better.spark.data.repository

import com.better.spark.domain.model.RelapseJournalEntry
import com.better.spark.domain.repository.RelapseJournalRepository
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

class RelapseJournalRepositoryImpl : RelapseJournalRepository {

    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val json = Json { ignoreUnknownKeys = true }
    private val _entriesFlow = MutableStateFlow<List<RelapseJournalEntry>>(emptyList())

    init {
        loadFromDefaults()
    }

    private fun loadFromDefaults() {
        val jsonString = userDefaults.stringForKey(KEY_ENTRIES)
        if (jsonString != null) {
            try {
                val entries = json.decodeFromString<List<RelapseJournalEntry>>(jsonString)
                _entriesFlow.value = entries
            } catch (e: Exception) {
                e.printStackTrace()
                _entriesFlow.value = emptyList()
            }
        }
    }

    private fun saveToDefaults(entries: List<RelapseJournalEntry>) {
        val jsonString = json.encodeToString(entries)
        userDefaults.setObject(jsonString, KEY_ENTRIES)
    }

    override fun observeEntries(): Flow<List<RelapseJournalEntry>> = _entriesFlow.asStateFlow()

    override suspend fun getAllEntries(): List<RelapseJournalEntry> = _entriesFlow.value

    override suspend fun addEntry(entry: RelapseJournalEntry) = withContext(Dispatchers.IO) {
        _entriesFlow.update { current ->
            val newList = current + entry
            saveToDefaults(newList)
            newList
        }
    }

    override suspend fun updateEntry(entry: RelapseJournalEntry) = withContext(Dispatchers.IO) {
        _entriesFlow.update { current ->
            val newList = current.map { if (it.id == entry.id) entry else it }
            saveToDefaults(newList)
            newList
        }
    }

    override suspend fun deleteEntry(entryId: String) = withContext(Dispatchers.IO) {
        _entriesFlow.update { current ->
            val newList = current.filterNot { it.id == entryId }
            saveToDefaults(newList)
            newList
        }
    }

    override suspend fun getEntryById(entryId: String): RelapseJournalEntry? {
        return _entriesFlow.value.find { it.id == entryId }
    }

    override suspend fun clearAll() = withContext(Dispatchers.IO) {
        _entriesFlow.value = emptyList()
        userDefaults.removeObjectForKey(KEY_ENTRIES)
    }

    private companion object {
        private const val KEY_ENTRIES = "key_relapse_journal_entries"
    }
}

