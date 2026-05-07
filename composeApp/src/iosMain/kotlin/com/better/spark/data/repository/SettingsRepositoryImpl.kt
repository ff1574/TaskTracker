package com.better.spark.data.repository

import com.better.spark.domain.repository.SettingsRepository
import platform.Foundation.NSUserDefaults
import platform.Foundation.removeObjectForKey

class SettingsRepositoryImpl : SettingsRepository {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override suspend fun saveDateOfBirth(timestamp: Long) {
        // TODO: Implement NSUserDefaults logic
    }

    override suspend fun getDateOfBirth(): Long? {
        return null // No DB for now
    }

    override suspend fun clearDateOfBirth() {
        // TODO
    }

    override suspend fun saveGender(gender: String) {}
    override suspend fun getGender(): String? = null
    override suspend fun saveSleepHours(hours: Float) {}
    override suspend fun getSleepHours(): Float? = null
    override suspend fun saveScreenTime(hours: Float) {}
    override suspend fun getScreenTime(): Float? = null
    
    override suspend fun saveSmoking(isSmoker: Boolean) {}
    override suspend fun getSmoking(): Boolean? = null
    override suspend fun saveAlcohol(drinksPerWeek: Int) {}
    override suspend fun getAlcohol(): Int? = null
    override suspend fun saveExercise(daysPerWeek: Int) {}
    override suspend fun getExercise(): Int? = null

    override suspend fun clearAll() {
        // Best effort, the iOS settings repo is a stub today.
        listOf(
            "key_date_of_birth",
            "key_gender",
            "key_sleep_hours_float",
            "key_screen_time",
            "key_smoking",
            "key_alcohol",
            "key_exercise"
        ).forEach { userDefaults.removeObjectForKey(it) }
    }
}
