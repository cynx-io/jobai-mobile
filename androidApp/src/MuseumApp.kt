package com.jetbrains.kmpapp

import android.app.Application
import com.jetbrains.kmpapp.di.initKoin
import org.koin.android.ext.koin.androidContext

class MuseumApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
        // Provide Android context to Koin
        org.koin.core.context.GlobalContext.get().androidContext(this)
    }
}
