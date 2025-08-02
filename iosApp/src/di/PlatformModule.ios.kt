package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.data.DataStoreFactory
import org.koin.dsl.module

actual val platformModule = module {
    single { DataStoreFactory() }
}