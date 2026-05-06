package com.umain.transport.di

import com.umain.transport.authorities.di.authoritiesModule
import com.umain.transport.core.di.coreModule
import com.umain.transport.departures.di.departuresModule
import com.umain.transport.lines.di.linesModule
import com.umain.transport.realtime.RealtimeConfig
import com.umain.transport.realtime.di.realtimeModule
import com.umain.transport.sites.di.sitesModule
import com.umain.transport.stoppoints.di.stopPointsModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * Bootstraps Koin with every feature module the library ships. The realtime
 * module needs a [RealtimeConfig] (simulator coordinates) — pass it
 * explicitly when calling, or let the default [RealtimeConfig.localhost]
 * point at the docker compose stack.
 */
fun initKoin(
    realtimeConfig: RealtimeConfig = RealtimeConfig.localhost(),
    appDeclaration: KoinAppDeclaration = {},
): KoinApplication {
    return startKoin {
        appDeclaration()
        modules(
            coreModule,
            linesModule,
            sitesModule,
            departuresModule,
            stopPointsModule,
            authoritiesModule,
            realtimeModule(realtimeConfig),
        )
    }
}

@JsExport
@JsName("initKoin")
fun initKoinForJs() {
    initKoin()
}

/**
 * JS bridge that lets browser/Node consumers point the realtime data layer
 * at a non-default simulator URL without dragging RealtimeConfig through
 * the @JsExport boundary.
 */
@JsExport
@JsName("initKoinWithRealtime")
fun initKoinWithRealtime(
    httpBaseUrl: String,
    wsHost: String,
    wsPort: Int,
    wsSecure: Boolean,
) {
    initKoin(
        realtimeConfig = RealtimeConfig(
            httpBaseUrl = httpBaseUrl,
            wsHost = wsHost,
            wsPort = wsPort,
            wsSecure = wsSecure,
        ),
    )
}
