package com.umain.transport.realtime.di

import com.umain.transport.realtime.RealtimeConfig
import com.umain.transport.realtime.data.remote.TripUpdateDataSource
import com.umain.transport.realtime.data.repository.TripRepositoryImpl
import com.umain.transport.realtime.domain.repository.TripRepository
import com.umain.transport.realtime.presentation.TripSelectionViewModel
import com.umain.transport.realtime.presentation.TripViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Realtime feature DI module. Built as a factory function rather than a
 * top-level `val` so callers must pass the [RealtimeConfig] (simulator
 * coordinates) — the talk-day flow rewrites `serverHostURL` per ngrok
 * session, so this can't be a hardcoded constant.
 *
 * Usage from a consumer's initKoin call:
 *   initKoin {
 *       modules(realtimeModule(RealtimeConfig.localhost()))
 *   }
 */
fun realtimeModule(config: RealtimeConfig): Module = module {
    single { config }
    single { TripUpdateDataSource(get(), get()) }
    single<TripRepository> { TripRepositoryImpl(get(), get(), get()) }
    factoryOf(::TripViewModel)
    factoryOf(::TripSelectionViewModel)
}
