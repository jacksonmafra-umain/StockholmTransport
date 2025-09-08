package com.umain.transport.sites.di

import com.umain.transport.sites.data.repository.SitesRepositoryImpl
import com.umain.transport.sites.domain.repository.SitesRepository
import com.umain.transport.sites.presentation.SitesViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val sitesModule =
    module {
        single<SitesRepository> { SitesRepositoryImpl(get()) }
        factoryOf(::SitesViewModel)
    }
