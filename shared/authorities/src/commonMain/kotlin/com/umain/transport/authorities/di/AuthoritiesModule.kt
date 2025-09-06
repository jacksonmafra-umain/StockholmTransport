package com.umain.transport.authorities.di

import com.umain.transport.authorities.data.repository.AuthoritiesRepositoryImpl
import com.umain.transport.authorities.domain.repository.AuthoritiesRepository
import com.umain.transport.authorities.presentation.AuthoritiesViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val authoritiesModule =
    module {
        single<AuthoritiesRepository> { AuthoritiesRepositoryImpl(get()) }
        factoryOf(::AuthoritiesViewModel)
    }
