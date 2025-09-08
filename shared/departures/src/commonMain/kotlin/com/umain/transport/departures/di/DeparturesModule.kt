package com.umain.transport.departures.di

import com.umain.transport.departures.data.repository.DeparturesRepositoryImpl
import com.umain.transport.departures.domain.repository.DeparturesRepository
import com.umain.transport.departures.presentation.DeparturesViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport

val departuresModule =
    module {
        single<DeparturesRepository> { DeparturesRepositoryImpl(get()) }
        factoryOf(::DeparturesViewModel)
    }
