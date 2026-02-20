package com.better.spark.domain.usecase

import com.better.spark.domain.model.Task
import com.better.spark.domain.repository.TaskRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for AddTaskUseCase.
 * 
 * Demonstrates TDD approach and best practices:
 * - Tests using fake repository (no real implementation needed)
 * - Uses runTest for coroutine testing
 * - Clear test names describing what is being tested
 * - Arrange-Act-Assert pattern
 */
class AddTaskUseCaseTest {
    
    @Test
    fun `addTask should add task to repository`() = runTest {
        // Arrange
        val repository = FakeTaskRepository()
        val useCase = AddTaskUseCase(repository)
        val task = Task(
            id = "1",
            title = "Test Task",
            description = "Test Description",
            createdAt = Clock.System.now()
        )
        
        // Act
        useCase(task)
        
        // Assert
        val tasks = repository.getAllTasks()
        assertEquals(1, tasks.size)
        assertEquals("Test Task", tasks.first().title)
    }
    
    @Test
    fun `addTask should preserve task properties`() = runTest {
        // Arrange
        val repository = FakeTaskRepository()
        val useCase = AddTaskUseCase(repository)
        val task = Task(
            id = "1",
            title = "Important Task",
            description = "This is important",
            isCompleted = false,
            createdAt = Clock.System.now()
        )
        
        // Act
        useCase(task)
        
        // Assert
        val savedTask = repository.getTaskById("1")!!
        assertEquals("Important Task", savedTask.title)
        assertEquals("This is important", savedTask.description)
        assertFalse(savedTask.isCompleted)
    }
    
    @Test
    fun `addTask should allow multiple tasks`() = runTest {
        // Arrange
        val repository = FakeTaskRepository()
        val useCase = AddTaskUseCase(repository)
        
        // Act
        useCase(Task("1", "Task 1", "", createdAt = Clock.System.now()))
        useCase(Task("2", "Task 2", "", createdAt = Clock.System.now()))
        useCase(Task("3", "Task 3", "", createdAt = Clock.System.now()))
        
        // Assert
        assertEquals(3, repository.getAllTasks().size)
    }
}
