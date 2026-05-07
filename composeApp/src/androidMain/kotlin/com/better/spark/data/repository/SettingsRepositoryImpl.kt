package com.better.spark.data.repository

import android.content.Context
import androidx.core.content.edit
import com.better.spark.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {

    private val prefs by lazy {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }

    override suspend fun saveDateOfBirth(timestamp: Long) = withContext(Dispatchers.IO) {
        prefs.edit { putLong(KEY_DOB, timestamp) }
    }

    override suspend fun getDateOfBirth(): Long? = withContext(Dispatchers.IO) {
        if (prefs.contains(KEY_DOB)) {
            prefs.getLong(KEY_DOB, 0L)
        } else {
            null
        }
    }

    override suspend fun clearDateOfBirth() = withContext(Dispatchers.IO) {
        prefs.edit { 
            remove(KEY_DOB)
            remove(KEY_GENDER)
            remove(KEY_SLEEP)
        }
    }

    override suspend fun saveGender(gender: String) = withContext(Dispatchers.IO) {
        prefs.edit { putString(KEY_GENDER, gender) }
    }

    override suspend fun getGender(): String? = withContext(Dispatchers.IO) {
        prefs.getString(KEY_GENDER, null)
    }

    override suspend fun saveSleepHours(hours: Float) = withContext(Dispatchers.IO) {
        prefs.edit { putFloat(KEY_SLEEP, hours) }
    }

    override suspend fun getSleepHours(): Float? = withContext(Dispatchers.IO) {
        if (prefs.contains(KEY_SLEEP)) prefs.getFloat(KEY_SLEEP, 8f) else null
    }

    override suspend fun saveScreenTime(hours: Float) = withContext(Dispatchers.IO) {
        prefs.edit { putFloat(KEY_SCREEN_TIME, hours) }
    }

    override suspend fun getScreenTime(): Float? = withContext(Dispatchers.IO) {
        if (prefs.contains(KEY_SCREEN_TIME)) prefs.getFloat(KEY_SCREEN_TIME, 2f) else null
    }

    override suspend fun saveSmoking(isSmoker: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit { putBoolean(KEY_SMOKING, isSmoker) }
    }

    override suspend fun getSmoking(): Boolean? = withContext(Dispatchers.IO) {
        if (prefs.contains(KEY_SMOKING)) prefs.getBoolean(KEY_SMOKING, false) else null
    }

    override suspend fun saveAlcohol(drinksPerWeek: Int) = withContext(Dispatchers.IO) {
        prefs.edit { putInt(KEY_ALCOHOL, drinksPerWeek) }
    }

    override suspend fun getAlcohol(): Int? = withContext(Dispatchers.IO) {
        if (prefs.contains(KEY_ALCOHOL)) prefs.getInt(KEY_ALCOHOL, 0) else null
    }

    override suspend fun saveExercise(daysPerWeek: Int) = withContext(Dispatchers.IO) {
        prefs.edit { putInt(KEY_EXERCISE, daysPerWeek) }
    }

    override suspend fun getExercise(): Int? = withContext(Dispatchers.IO) {
        if (prefs.contains(KEY_EXERCISE)) prefs.getInt(KEY_EXERCISE, 3) else null
    }

    override suspend fun clearAll() = withContext(Dispatchers.IO) {
        prefs.edit { clear() }
    }

    companion object {
        private const val KEY_DOB = "key_date_of_birth"
        private const val KEY_GENDER = "key_gender"
        private const val KEY_SLEEP = "key_sleep_hours_float"
        private const val KEY_SCREEN_TIME = "key_screen_time"
        private const val KEY_SMOKING = "key_smoking"
        private const val KEY_ALCOHOL = "key_alcohol"
        private const val KEY_EXERCISE = "key_exercise"
    }
}
