package com.umain.transport.app

import android.app.Application
import com.umain.transport.app.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        com.umain.transport.di.initKoin {
            androidLogger()
            androidContext(this@MainApplication)
        }
    }
}