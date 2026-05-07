package com.better.spark.di

import com.better.spark.domain.repository.TaskRepository
import com.better.spark.domain.usecase.AddTaskUseCase
import com.better.spark.domain.usecase.ArchiveTaskUseCase
import com.better.spark.domain.usecase.GetAllTasksUseCase
import com.better.spark.domain.usecase.GetBadHabitsUseCase
import com.better.spark.domain.usecase.ToggleTaskCompleteUseCase
import com.better.spark.domain.usecase.UpdateTaskUseCase
import com.better.spark.domain.usecase.DeleteTaskUseCase
import com.better.spark.domain.usecase.UnarchiveTaskUseCase
import com.better.spark.presentation.viewmodel.BadHabitViewModel
import com.better.spark.presentation.viewmodel.LifeCalendarViewModel
import com.better.spark.presentation.viewmodel.RelapseJournalViewModel
import com.better.spark.presentation.viewmodel.TaskViewModel
import kotlinx.datetime.Clock
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin dependency injection module for the application.
 * 
 * Best practices:
 * - Uses singleOf for concise singleton declaration
 * - Uses viewModelOf for ViewModel injection
 * - Binds implementations to interfaces (dependency inversion)
 * - Organized by layer (data, domain, presentation)
 * - Easy to test by swapping implementations
 */
val appModule = module {
    includes(platformModule)

    // Core
    single<Clock> { Clock.System }

    // Data Layer - Repository
    // Data Layer - Repository
    // singleOf(::TaskRepositoryImpl) bind TaskRepository::class // Moved to PlatformModule
    
    // Domain Layer - Use Cases
    singleOf(::GetAllTasksUseCase)
    singleOf(::GetBadHabitsUseCase)
    singleOf(::AddTaskUseCase)
    singleOf(::ToggleTaskCompleteUseCase)
    singleOf(::UpdateTaskUseCase)
    singleOf(::DeleteTaskUseCase)
    singleOf(::ArchiveTaskUseCase)
    singleOf(::UnarchiveTaskUseCase)
    
    // Presentation Layer - ViewModels
    viewModelOf(::TaskViewModel)
    viewModelOf(::BadHabitViewModel)
    viewModelOf(::LifeCalendarViewModel)
    viewModelOf(::RelapseJournalViewModel)
}
