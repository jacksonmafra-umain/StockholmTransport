package com.umain.transport.core.di

import com.umain.transport.authorities.di.authoritiesModule
import com.umain.transport.core.network.createHttpClient
import com.umain.transport.departures.di.departuresModule
import com.umain.transport.lines.di.linesModule
import com.umain.transport.sites.di.sitesModule
import com.umain.transport.stoppoints.di.stopPointsModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            coreModule,
            linesModule,
            sitesModule,
            departuresModule,
            stopPointsModule,
            authoritiesModule
        )
    }
}

val coreModule = module {
    single { createHttpClient() }
}