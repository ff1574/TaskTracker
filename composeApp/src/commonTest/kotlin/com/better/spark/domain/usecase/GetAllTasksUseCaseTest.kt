package com.better.spark.domain.usecase

import com.better.spark.domain.model.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetAllTasksUseCaseTest {

    private var fixedTime = Instant.parse("2026-02-11T20:00:00Z")
    private val fakeClock = object : Clock {
        override fun now() = fixedTime
    }

    @Test
    fun `invoke should return tasks from repository`() = runTest {
        val repository = FakeTaskRepository()
        val useCase = GetAllTasksUseCase(repository, fakeClock)
        
        repository.addTask(Task("1", "Task 1", createdAt = fixedTime))
        
        val tasks = useCase().first()
        assertEquals(1, tasks.size)
        assertEquals("Task 1", tasks[0].title)
    }

    @Test
    fun `invoke should reset expired repeatable tasks`() = runTest {
        // Arrange
        val repository = FakeTaskRepository()
        val useCase = GetAllTasksUseCase(repository, fakeClock)
        
        val interval = 60 * 1000L // 1 minute
        val completedAt = fixedTime.minus(interval + 1, kotlinx.datetime.DateTimeUnit.MILLISECOND) // Expired by 1ms
        
        val expiredTask = Task(
            id = "1", 
            title = "Expired Task", 
            createdAt = fixedTime,
            isCompleted = true,
            completedAt = completedAt,
            repeatInterval = interval
        )
        
        repository.addTask(expiredTask)
        
        // Act
        // wait for second emission which reflects the reset
        val tasks = useCase().take(2).toList().last()
        
        // Assert
        assertEquals(1, tasks.size)
        val task = tasks[0]
        assertFalse(task.isCompleted, "Task should be reset to incomplete")
        assertNull(task.completedAt, "CompletedAt should be null")
    }

    @Test
    fun `invoke should NOT reset unexpired repeatable tasks`() = runTest {
        // Arrange
        val repository = FakeTaskRepository()
        val useCase = GetAllTasksUseCase(repository, fakeClock)
        
        val interval = 60 * 1000L // 1 minute
        val completedAt = fixedTime.minus(interval - 10000, kotlinx.datetime.DateTimeUnit.MILLISECOND) // Not expired yet
        
        val activeTask = Task(
            id = "1", 
            title = "Active Task", 
            createdAt = fixedTime,
            isCompleted = true,
            completedAt = completedAt,
            repeatInterval = interval
        )
        
        repository.addTask(activeTask)
        
        // Act
        // Unexpired tasks don't get reset, so we'll only get 1 emission immediately.
        val tasks = useCase().first()
        
        // Assert
        assertEquals(1, tasks.size)
        val task = tasks[0]
        assertTrue(task.isCompleted, "Task should remain completed")
        assertNotNull(task.completedAt)
    }

    @Test
    fun `invoke should reset task on matching repeat day`() = runTest {
        // Arrange
        val repository = FakeTaskRepository()
        val useCase = GetAllTasksUseCase(repository, fakeClock)
        
        // Fixed Time is Wed Feb 11 2026 UTC, but locally it could be Thu Feb 12.
        // We must calculate the correct local DayOfWeek for "today".
        val tz = TimeZone.currentSystemDefault()
        val nowDate = fixedTime.toLocalDateTime(tz).date
        val todayIso = nowDate.dayOfWeek.ordinal + 1 // 1=Mon...
        
        val yesterday = fixedTime.minus(1, kotlinx.datetime.DateTimeUnit.DAY, tz)
        
        val task = Task(
            id = "1", 
            title = "Dynamic Day Task", 
            createdAt = yesterday,
            isCompleted = true,
            completedAt = yesterday,
            repeatDays = listOf(todayIso) // Dynamically set to "today"
        )
        
        repository.addTask(task)
        
        // Act
        // Since we explicitly made today a repeat day, it SHOULD trigger a reset and thus a 2nd emission.
        val tasks = useCase().take(2).toList().last()
        
        // Assert
        assertEquals(1, tasks.size)
        assertFalse(tasks[0].isCompleted, "Task should be reset")
    }

    @Test
    fun `invoke should NOT reset task if completed today even if today is repeat day`() = runTest {
        // Arrange
        val repository = FakeTaskRepository()
        val useCase = GetAllTasksUseCase(repository, fakeClock)
        
        // Completed TODAY
        // Should not reset until NEXT occurrence
        
        val tz = TimeZone.currentSystemDefault()
        val nowDate = fixedTime.toLocalDateTime(tz).date
        val todayIso = nowDate.dayOfWeek.ordinal + 1
        
        val task = Task(
            id = "1", 
            title = "Wed Task", 
            createdAt = fixedTime,
            isCompleted = true,
            completedAt = fixedTime,
            repeatDays = listOf(todayIso)
        )
        
        repository.addTask(task)
        
        // Act
        val tasks = useCase().first()
        
        // Assert
        assertEquals(1, tasks.size)
        assertTrue(tasks[0].isCompleted, "Task should stay completed today")
    }
}
