package com.better.spark.di

import com.better.spark.data.repository.SettingsRepositoryImpl
import com.better.spark.data.repository.TaskRepositoryImpl
import com.better.spark.domain.repository.SettingsRepository
import com.better.spark.domain.repository.TaskRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule: Module = module {
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
    singleOf(::TaskRepositoryImpl) bind TaskRepository::class
}
