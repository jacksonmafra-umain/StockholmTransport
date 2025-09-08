package com.umain.transport.core.di

import com.umain.transport.core.network.createHttpClient
import org.koin.dsl.module
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
val coreModule =
    module {
        single { createHttpClient() }
    }
