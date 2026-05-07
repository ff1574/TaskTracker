package com.better.spark.data.repository

import android.content.Context
import androidx.core.content.edit
import com.better.spark.domain.model.RelapseJournalEntry
import com.better.spark.domain.repository.RelapseJournalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RelapseJournalRepositoryImpl(
    private val context: Context
) : RelapseJournalRepository {

    private val prefs by lazy {
        context.getSharedPreferences("relapse_journal", Context.MODE_PRIVATE)
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val _entriesFlow = MutableStateFlow<List<RelapseJournalEntry>>(emptyList())

    init {
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        val jsonString = prefs.getString(KEY_ENTRIES, null)
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

    private fun saveToPrefs(entries: List<RelapseJournalEntry>) {
        val jsonString = json.encodeToString(entries)
        prefs.edit { putString(KEY_ENTRIES, jsonString) }
    }

    override fun observeEntries(): Flow<List<RelapseJournalEntry>> = _entriesFlow.asStateFlow()

    override suspend fun getAllEntries(): List<RelapseJournalEntry> = _entriesFlow.value

    override suspend fun addEntry(entry: RelapseJournalEntry) = withContext(Dispatchers.IO) {
        _entriesFlow.update { current ->
            val newList = current + entry
            saveToPrefs(newList)
            newList
        }
    }

    override suspend fun updateEntry(entry: RelapseJournalEntry) = withContext(Dispatchers.IO) {
        _entriesFlow.update { current ->
            val newList = current.map { if (it.id == entry.id) entry else it }
            saveToPrefs(newList)
            newList
        }
    }

    override suspend fun deleteEntry(entryId: String) = withContext(Dispatchers.IO) {
        _entriesFlow.update { current ->
            val newList = current.filterNot { it.id == entryId }
            saveToPrefs(newList)
            newList
        }
    }

    override suspend fun getEntryById(entryId: String): RelapseJournalEntry? {
        return _entriesFlow.value.find { it.id == entryId }
    }

    override suspend fun clearAll() = withContext(Dispatchers.IO) {
        _entriesFlow.value = emptyList()
        prefs.edit { remove(KEY_ENTRIES) }
    }

    private companion object {
        private const val KEY_ENTRIES = "key_relapse_journal_entries"
    }
}

