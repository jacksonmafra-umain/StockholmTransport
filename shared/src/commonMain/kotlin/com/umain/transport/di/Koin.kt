package com.umain.transport.di

import com.umain.transport.authorities.di.authoritiesModule
import com.umain.transport.core.di.coreModule
import com.umain.transport.departures.di.departuresModule
import com.umain.transport.lines.di.linesModule
import com.umain.transport.sites.di.sitesModule
import com.umain.transport.stoppoints.di.stopPointsModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import kotlin.js.JsExport
import kotlin.js.JsName

fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication {
    return startKoin {
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

@JsExport
@JsName("initKoin")
fun initKoinForJs() {
    initKoin()
}