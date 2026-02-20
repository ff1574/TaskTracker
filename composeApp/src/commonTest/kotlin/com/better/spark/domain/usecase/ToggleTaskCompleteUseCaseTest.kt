package com.better.spark.domain.usecase

import com.better.spark.domain.model.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for ToggleTaskCompleteUseCase.
 * 
 * Tests the business logic for toggling task completion status.
 */
class ToggleTaskCompleteUseCaseTest {
    
    private val fixedTime = Instant.parse("2026-02-11T20:00:00Z")
    private val fakeClock = object : kotlinx.datetime.Clock {
        override fun now() = fixedTime
    }
    
    @Test
    fun `toggleComplete should mark incomplete task as complete`() = runTest {
        // Arrange
        val repository = FakeTaskRepository()
        val useCase = ToggleTaskCompleteUseCase(repository, fakeClock)
        val task = Task(
            id = "1",
            title = "Test Task",
            createdAt = Clock.System.now(),
            isCompleted = false
        )
        repository.addTask(task)
        
        // Act
        useCase("1")
        
        // Assert
        val updatedTask = repository.getTaskById("1")!!
        assertTrue(updatedTask.isCompleted)
        assertNotNull(updatedTask.completedAt)
        assertEquals(fixedTime, updatedTask.completedAt)
    }
    
    @Test
    fun `toggleComplete should mark complete task as incomplete`() = runTest {
        // Arrange
        val repository = FakeTaskRepository()
        val useCase = ToggleTaskCompleteUseCase(repository, fakeClock)
        val task = Task(
            id = "1",
            title = "Test Task",
            createdAt = Clock.System.now(),
            isCompleted = true,
            completedAt = fixedTime
        )
        repository.addTask(task)
        
        // Act
        useCase("1")
        
        // Assert
        val updatedTask = repository.getTaskById("1")!!
        assertTrue(!updatedTask.isCompleted)
        assertNull(updatedTask.completedAt)
    }
    
    @Test
    fun `toggleComplete should emit updated task through flow`() = runTest {
        // Arrange
        val repository = FakeTaskRepository()
        val useCase = ToggleTaskCompleteUseCase(repository, fakeClock)
        val task = Task(
            id = "1",
            title = "Test Task",
            createdAt = Clock.System.now(),
            isCompleted = false
        )
        repository.addTask(task)
        
        // Act
        useCase("1")
        
        // Assert
        val flowTasks = repository.observeTasks().first()
        assertEquals(1, flowTasks.size)
        assertTrue(flowTasks.first().isCompleted)
    }
}
