package com.umain.transport.departures.di

import com.umain.transport.departures.data.repository.DeparturesRepositoryImpl
import com.umain.transport.departures.domain.repository.DeparturesRepository
import com.umain.transport.departures.presentation.DeparturesViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val departuresModule =
    module {
        single<DeparturesRepository> { DeparturesRepositoryImpl(get()) }
        factoryOf(::DeparturesViewModel)
    }
