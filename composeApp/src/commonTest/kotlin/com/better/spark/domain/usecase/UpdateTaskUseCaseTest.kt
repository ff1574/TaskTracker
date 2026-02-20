package com.better.spark.domain.usecase

import com.better.spark.domain.model.Task
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UpdateTaskUseCaseTest {

    private val fakeClock = object : Clock {
        override fun now() = kotlinx.datetime.Instant.parse("2024-01-01T12:00:00Z")
    }

    @Test
    fun `invoke should update task in repository`() = runTest {
        // Arrange
        val repository = FakeTaskRepository()
        val useCase = UpdateTaskUseCase(repository)
        
        val task = Task(
            id = "1",
            title = "Original Title",
            description = "Original Desc",
            isCompleted = false,
            createdAt = fakeClock.now()
        )
        repository.addTask(task)

        val updatedTask = task.copy(title = "Updated Title", description = "Updated Desc")

        // Act
        useCase(updatedTask)

        // Assert
        val retrievedTask = repository.getTaskById("1")
        assertNotNull(retrievedTask)
        assertEquals("Updated Title", retrievedTask!!.title)
        assertEquals("Updated Desc", retrievedTask!!.description)
    }
}
