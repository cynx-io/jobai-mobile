package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.data.AuthApi
import com.jetbrains.kmpapp.data.AuthRepository
import com.jetbrains.kmpapp.data.AuthStorage
import com.jetbrains.kmpapp.data.DataStoreAuthStorage
import com.jetbrains.kmpapp.data.DataStoreFactory
import com.jetbrains.kmpapp.data.InMemoryMuseumStorage
import com.jetbrains.kmpapp.data.KtorAuthApi
import com.jetbrains.kmpapp.data.KtorMuseumApi
import com.jetbrains.kmpapp.data.MuseumApi
import com.jetbrains.kmpapp.data.MuseumRepository
import com.jetbrains.kmpapp.data.MuseumStorage
import com.jetbrains.kmpapp.screens.auth.AuthScreenModel
import com.jetbrains.kmpapp.screens.detail.DetailScreenModel
import com.jetbrains.kmpapp.screens.list.ListScreenModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val dataModule = module {
    single {
        val json = Json { ignoreUnknownKeys = true }
        HttpClient {
            install(ContentNegotiation) {
                // TODO Fix API so it serves application/json
                json(json, contentType = ContentType.Any)
            }
        }
    }

    // Museum related
    single<MuseumApi> { KtorMuseumApi(get()) }
    single<MuseumStorage> { InMemoryMuseumStorage() }
    single {
        MuseumRepository(get(), get()).apply {
            initialize()
        }
    }
    
    // Auth related
    single<AuthApi> { KtorAuthApi(get()) }
    single { get<DataStoreFactory>().createDataStore() }
    single<AuthStorage> { DataStoreAuthStorage(get(), get()) }
    single { AuthRepository(get(), get()) }
}

val screenModelsModule = module {
    factoryOf(::ListScreenModel)
    factoryOf(::DetailScreenModel)
    factoryOf(::AuthScreenModel)
}

fun initKoin() {
    startKoin {
        modules(
            platformModule,
            dataModule,
            screenModelsModule,
        )
    }
}
