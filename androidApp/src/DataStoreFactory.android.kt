package com.jetbrains.kmpapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

actual class DataStoreFactory(private val context: Context) {
    actual fun createDataStore(): DataStore<Preferences> = context.authDataStore
}

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")