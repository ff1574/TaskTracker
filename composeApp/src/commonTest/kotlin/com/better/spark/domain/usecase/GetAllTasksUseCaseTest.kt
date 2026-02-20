package com.better.spark.domain.usecase

import com.better.spark.domain.model.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.TimeZone
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
        // The use case flow should trigger the reset logic on collection
        val tasks = useCase().first()
        
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
        val tasks = useCase().first()
        
        // Assert
        assertEquals(1, tasks.size)
        val task = tasks[0]
        assertTrue(task.isCompleted, "Task should remain completed")
        assertNotNull(task.completedAt)
        @Test
    fun `invoke should reset task on matching repeat day`() = runTest {
        // Arrange
        val repository = FakeTaskRepository()
        val useCase = GetAllTasksUseCase(repository, fakeClock)
        
        // Fixed Time is Wed Feb 11 2026. ordinal=2 (Monday=0)
        // Let's say task completed yesterday (Tue) and repeats on Wed (Today)
        
        val yesterday = fixedTime.minus(1, kotlinx.datetime.DateTimeUnit.DAY, kotlinx.datetime.TimeZone.UTC)
        
        val task = Task(
            id = "1", 
            title = "Wed Task", 
            createdAt = yesterday,
            isCompleted = true,
            completedAt = yesterday,
            repeatDays = listOf(3) // 3 = Wednesday (1=Mon, 2=Tue, 3=Wed)
        )
        
        repository.addTask(task)
        
        // Act
        val tasks = useCase().first()
        
        // Assert
        assertEquals(1, tasks.size)
        // Should reset because today is Wednesday and completion was Tuesday
        assertFalse(tasks[0].isCompleted, "Task should be reset")
    }

    @Test
    fun `invoke should NOT reset task if completed today even if today is repeat day`() = runTest {
        // Arrange
        val repository = FakeTaskRepository()
        val useCase = GetAllTasksUseCase(repository, fakeClock)
        
        // Completed TODAY (Wed)
        // Should not reset until NEXT occurrence (Next Wed)
        
        val task = Task(
            id = "1", 
            title = "Wed Task", 
            createdAt = fixedTime,
            isCompleted = true,
            completedAt = fixedTime,
            repeatDays = listOf(3) // 3 = Wednesday
        )
        
        repository.addTask(task)
        
        // Act
        val tasks = useCase().first()
        
        // Assert
        assertEquals(1, tasks.size)
        assertTrue(tasks[0].isCompleted, "Task should stay completed today")
    }
}
}
