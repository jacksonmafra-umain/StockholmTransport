package com.umain.transport.lines.di

import com.umain.transport.lines.data.repository.LinesRepositoryImpl
import com.umain.transport.lines.domain.repository.LinesRepository
import com.umain.transport.lines.presentation.LinesViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
val linesModule =
    module {
        single<LinesRepository> { LinesRepositoryImpl(get()) }
        factoryOf(::LinesViewModel)
    }
