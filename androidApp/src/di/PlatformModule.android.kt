package com.jetbrains.kmpapp.di

import android.content.Context
import com.jetbrains.kmpapp.data.DataStoreFactory
import org.koin.dsl.module

actual val platformModule = module {
    single { DataStoreFactory(get<Context>()) }
}