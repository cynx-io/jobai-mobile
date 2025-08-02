package com.jetbrains.kmpapp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect class DataStoreFactory {
    fun createDataStore(): DataStore<Preferences>
}