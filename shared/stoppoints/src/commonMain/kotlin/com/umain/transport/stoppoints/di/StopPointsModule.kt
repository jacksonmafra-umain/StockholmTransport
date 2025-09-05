package com.umain.transport.stoppoints.di

import com.umain.transport.stoppoints.data.repository.StopPointsRepositoryImpl
import com.umain.transport.stoppoints.domain.repository.StopPointsRepository
import com.umain.transport.stoppoints.presentation.StopPointsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val stopPointsModule = module {
    single<StopPointsRepository> { StopPointsRepositoryImpl(get()) }
    factoryOf(::StopPointsViewModel)
}