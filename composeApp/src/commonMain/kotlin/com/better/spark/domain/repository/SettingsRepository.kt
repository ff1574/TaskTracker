package com.better.spark.domain.repository

interface SettingsRepository {
    suspend fun saveDateOfBirth(timestamp: Long)
    suspend fun getDateOfBirth(): Long?
    suspend fun clearDateOfBirth()
    
    suspend fun saveGender(gender: String) // "Male", "Female", "Other"
    suspend fun getGender(): String?
    
    suspend fun saveSleepHours(hours: Float)
    suspend fun getSleepHours(): Float?
    
    suspend fun saveScreenTime(hours: Float)
    suspend fun getScreenTime(): Float?
    
    // Habits
    suspend fun saveSmoking(isSmoker: Boolean)
    suspend fun getSmoking(): Boolean?
    
    suspend fun saveAlcohol(drinksPerWeek: Int)
    suspend fun getAlcohol(): Int?
    
    suspend fun saveExercise(daysPerWeek: Int)
    suspend fun getExercise(): Int?

    /** Clears all locally stored app settings. */
    suspend fun clearAll()
}
