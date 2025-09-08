package com.umain.transport.app.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
   // modules(
   //     dataModule,
   //     presentationModule
   // )
}

val dataModule = module {

}

val presentationModule = module {

}