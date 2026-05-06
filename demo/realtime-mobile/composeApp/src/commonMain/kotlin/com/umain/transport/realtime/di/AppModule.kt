package com.umain.transport.realtime.di

import com.umain.transport.realtime.data.remote.TripUpdateDataSource
import com.umain.transport.realtime.data.remote.provideHttpClient
import com.umain.transport.realtime.data.repository.TripRepositoryImpl
import com.umain.transport.realtime.domain.repository.TripRepository
import com.umain.transport.realtime.presentation.TripSelectionViewModel
import com.umain.transport.realtime.presentation.TripViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(
        dataModule,
        presentationModule
    )
}

val dataModule = module {
    single { provideHttpClient() }
    single { TripUpdateDataSource(get()) }
    single<TripRepository> { TripRepositoryImpl(dataSource = get(), httpClient = get()) }
}

val presentationModule = module {
    factory {
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        TripViewModel(get(), scope)
    }

    factory {
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        TripSelectionViewModel(get(), scope)
    }
}