package com.umain.transport.core.di

import com.umain.transport.core.network.createHttpClient
import org.koin.dsl.module

val coreModule = module {
    single { createHttpClient() }
}