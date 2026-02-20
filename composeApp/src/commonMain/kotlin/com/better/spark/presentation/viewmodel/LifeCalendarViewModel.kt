package com.better.spark.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.better.spark.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

sealed interface LifeCalendarState {
    data object Loading : LifeCalendarState
    data object Onboarding : LifeCalendarState
    data class Display(
        val weeksLived: Int,
        val weeksSleep: Int,
        val weeksScreenTime: Int,
        val weeksAwake: Int,
        val totalWeeks: Int,
        val lifeExpectancyYears: Int,
        // Config for Editing
        val dobMillis: Long,
        val gender: String,
        val sleepHours: Float,
        val screenHours: Float,
        val isSmoker: Boolean,
        val alcoholDrinks: Int,
        val exerciseDays: Int
    ) : LifeCalendarState
}

class LifeCalendarViewModel(
    private val settingsRepository: SettingsRepository,
    private val clock: Clock
) : ViewModel() {

    private val _state = MutableStateFlow<LifeCalendarState>(LifeCalendarState.Loading)
    val state: StateFlow<LifeCalendarState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val dob = settingsRepository.getDateOfBirth()
            val gender = settingsRepository.getGender()
            val sleep = settingsRepository.getSleepHours()
            val screen = settingsRepository.getScreenTime()
            
            // Habits
            val smoking = settingsRepository.getSmoking()
            val alcohol = settingsRepository.getAlcohol()
            val exercise = settingsRepository.getExercise()

            if (dob == null || gender == null || sleep == null || screen == null || smoking == null || alcohol == null || exercise == null) {
                _state.value = LifeCalendarState.Onboarding
            } else {
                calculateWeeks(dob, gender, sleep, screen, smoking, alcohol, exercise)
            }
        }
    }

    fun saveOnboardingData(
        epochMillis: Long, 
        gender: String, 
        sleepHours: Float, 
        screenHours: Float,
        isSmoker: Boolean,
        alcoholDrinks: Int,
        exerciseDays: Int
    ) {
        viewModelScope.launch {
            settingsRepository.saveDateOfBirth(epochMillis)
            settingsRepository.saveGender(gender)
            settingsRepository.saveSleepHours(sleepHours)
            settingsRepository.saveScreenTime(screenHours)
            
            settingsRepository.saveSmoking(isSmoker)
            settingsRepository.saveAlcohol(alcoholDrinks)
            settingsRepository.saveExercise(exerciseDays)
            
            calculateWeeks(epochMillis, gender, sleepHours, screenHours, isSmoker, alcoholDrinks, exerciseDays)
        }
    }
    
    fun reset() {
        viewModelScope.launch {
            settingsRepository.clearDateOfBirth()
            _state.value = LifeCalendarState.Onboarding
        }
    }

    private fun calculateWeeks(
        dobMillis: Long, 
        gender: String, 
        sleepHours: Float, 
        screenHours: Float,
        isSmoker: Boolean,
        alcoholDrinks: Int,
        exerciseDays: Int
    ) {
        val now = clock.now().toEpochMilliseconds()
        val diffMillis = now - dobMillis
        
        val weeksLived = (diffMillis / (1000L * 60 * 60 * 24 * 7)).toInt()
        
        // --- Calculate Expectancy ---
        var expectancyYears = when (gender.lowercase()) {
            "male" -> 76
            "female" -> 81
            else -> 79
        }
        
        // Harsher Reality Check
        if (isSmoker) expectancyYears -= 7
        if (alcoholDrinks > 10) expectancyYears -= 3
        if (exerciseDays < 2) expectancyYears -= 2 // Sedentary
        if (exerciseDays >= 4) expectancyYears += 2 // Active
        
        // -----------------------------
        
        val totalWeeks = expectancyYears * 52
        val clampedLived = weeksLived.coerceIn(0, totalWeeks)
        val remainingWeeks = totalWeeks - clampedLived
        
        val sleepRatio = sleepHours / 24f
        val sleepWeeks = (remainingWeeks * sleepRatio).toInt()

        val screenRatio = screenHours / 24f 
        val screenWeeks = (remainingWeeks * screenRatio).toInt()
        
        val awakeWeeks = (remainingWeeks - sleepWeeks - screenWeeks).coerceAtLeast(0)

        _state.value = LifeCalendarState.Display(
            weeksLived = clampedLived,
            weeksSleep = sleepWeeks,
            weeksScreenTime = screenWeeks,
            weeksAwake = awakeWeeks,
            totalWeeks = totalWeeks,
            lifeExpectancyYears = expectancyYears,
            dobMillis = dobMillis,
            gender = gender,
            sleepHours = sleepHours,
            screenHours = screenHours,
            isSmoker = isSmoker,
            alcoholDrinks = alcoholDrinks,
            exerciseDays = exerciseDays
        )
    }
}
