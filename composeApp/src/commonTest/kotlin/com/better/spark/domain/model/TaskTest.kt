package com.better.spark.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TaskTest {

    @Test
    fun `getStreakDays with zero relapses returns days since creation`() {
        // Arrange
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        
        // Task created exactly 10 days ago, no relapses (empty completionHistory)
        val creationDate = today.minus(10, DateTimeUnit.DAY)
        // Convert local date back to an instant (using start of day just for setup)
        val creationInstant = kotlinx.datetime.LocalDateTime(
            creationDate.year, creationDate.monthNumber, creationDate.dayOfMonth, 0, 0
        ).toInstant(tz)
        
        val task = Task(
            id = "test-1",
            title = "Quit Smoking",
            createdAt = creationInstant,
            isBadHabit = true,
            badHabitType = BadHabitType.SMOKING,
            completionHistory = emptyList()
        )

        // Act
        val streak = task.getStreakDays(today)

        // Assert
        assertEquals(10, streak)
    }

    @Test
    fun `getStreakDays with relapses returns days since most recent relapse`() {
        // Arrange
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        
        // Task created 30 days ago
        val creationDate = today.minus(30, DateTimeUnit.DAY)
        val creationInstant = kotlinx.datetime.LocalDateTime(
            creationDate.year, creationDate.monthNumber, creationDate.dayOfMonth, 0, 0
        ).toInstant(tz)
        
        // Relapse happened 3 days ago. For bad habits, completionHistory stores YYYY-MM-DD
        val relapseDate = today.minus(3, DateTimeUnit.DAY)
        
        // Also add an older relapse 12 days ago to ensure it picks the most RECENT one
        val oldRelapseDate = today.minus(12, DateTimeUnit.DAY)
        
        val task = Task(
            id = "test-2",
            title = "Drink Less",
            createdAt = creationInstant,
            isBadHabit = true,
            badHabitType = BadHabitType.ALCOHOL,
            completionHistory = listOf(oldRelapseDate.toString(), relapseDate.toString())
        )

        // Act
        val streak = task.getStreakDays(today)

        // Assert
        assertEquals(3, streak) // Streak is 3 days
    }

    @Test
    fun `getStreakDays with relapse today returns exactly zero`() {
        // Arrange
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        
        val creationDate = today.minus(5, DateTimeUnit.DAY)
        val creationInstant = kotlinx.datetime.LocalDateTime(
            creationDate.year, creationDate.monthNumber, creationDate.dayOfMonth, 0, 0
        ).toInstant(tz)
        
        val task = Task(
            id = "test-3",
            title = "Stop Biting Nails",
            createdAt = creationInstant,
            isBadHabit = true,
            badHabitType = BadHabitType.CUSTOM,
            completionHistory = listOf(today.toString()) // Relapsed today!
        )

        // Act
        val streak = task.getStreakDays(today)

        // Assert
        assertEquals(0, streak) // Streak should be destroyed (0 days)
    }
}
