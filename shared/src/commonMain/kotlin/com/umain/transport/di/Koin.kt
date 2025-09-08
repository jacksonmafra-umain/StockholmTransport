package com.umain.transport.di

import com.umain.transport.authorities.di.authoritiesModule
import com.umain.transport.core.di.coreModule
import com.umain.transport.departures.di.departuresModule
import com.umain.transport.lines.di.linesModule
import com.umain.transport.sites.di.sitesModule
import com.umain.transport.stoppoints.di.stopPointsModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * Internal entry point for Kotlin consumers (like composeApp).
 * It allows passing extra configurations.
 * It is NOT exported to JS.
 */
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

/**
 * Public, parameter-less entry point for JavaScript consumers.
 * It is exported to JS with a clean signature.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("initKoin")
fun initKoinForJs() {
    initKoin() // Calls the internal function with default parameters
}