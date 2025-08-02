package com.jetbrains.kmpapp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class DataStoreFactory {
    @OptIn(InternalCoroutinesApi::class)
    actual fun createDataStore(): DataStore<Preferences> = synchronized(lock) {
        if (dataStore == null) {
            dataStore = PreferenceDataStoreFactory.createWithPath(
                produceFile = { 
                    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
                        directory = NSDocumentDirectory,
                        inDomain = NSUserDomainMask,
                        appropriateForURL = null,
                        create = false,
                        error = null
                    )
                    requireNotNull(documentDirectory).path + "/auth_prefs.preferences_pb"
                }.toPath()
            )
        }
        return dataStore!!
    }
    
    companion object {
        @OptIn(InternalCoroutinesApi::class)
        private val lock = SynchronizedObject()
        private var dataStore: DataStore<Preferences>? = null
    }
}