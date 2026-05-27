package com.umain.transport.realtime.di

import com.umain.transport.di.initKoin as libraryInitKoin
import com.umain.transport.realtime.RealtimeConfig
import com.umain.transport.realtime.config.BuildConfig
import org.koin.dsl.KoinAppDeclaration

/**
 * Bootstraps the realtime-mobile demo by delegating to the
 * `:stockholm-transport` library's [com.umain.transport.di.initKoin]. Every
 * binding the demo needs (HttpClient, TripUpdateDataSource, TripRepository,
 * TripViewModel, TripSelectionViewModel) ships in the library's
 * `realtimeModule(...)` after Option C — the demo no longer carries its own
 * data / domain / repository / viewmodel duplicates.
 *
 * Realtime simulator coordinates come from `BuildConfig` (Android emulator
 * uses 10.0.2.2; iOS simulator uses 127.0.0.1; talk-day ngrok rewrites these
 * via `./sl start`).
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) = libraryInitKoin(
    realtimeConfig = RealtimeConfig(
        httpBaseUrl = BuildConfig.SERVER_HOST_URL,
        wsHost = BuildConfig.SERVER_HOST,
        wsPort = BuildConfig.SERVER_PORT,
        wsSecure = BuildConfig.SERVER_HOST_URL.startsWith("https://"),
    ),
    appDeclaration = appDeclaration,
)
