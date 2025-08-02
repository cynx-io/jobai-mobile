package com.jetbrains.kmpapp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface AuthStorage {
    suspend fun saveAuthData(user: AuthUser, tokens: AuthTokens)
    suspend fun getAuthUser(): AuthUser?
    suspend fun getAuthTokens(): AuthTokens?
    suspend fun clearAuthData()
    fun isLoggedIn(): Flow<Boolean>
}

class DataStoreAuthStorage(
    private val dataStore: DataStore<Preferences>,
    private val json: Json = Json
) : AuthStorage {
    
    companion object {
        private val USER_KEY = stringPreferencesKey("auth_user")
        private val TOKENS_KEY = stringPreferencesKey("auth_tokens")
    }
    
    override suspend fun saveAuthData(user: AuthUser, tokens: AuthTokens) {
        dataStore.edit { preferences ->
            preferences[USER_KEY] = json.encodeToString(user)
            preferences[TOKENS_KEY] = json.encodeToString(tokens)
        }
    }
    
    override suspend fun getAuthUser(): AuthUser? {
        return try {
            dataStore.data.map { preferences ->
                preferences[USER_KEY]?.let { json.decodeFromString<AuthUser>(it) }
            }.first()
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getAuthTokens(): AuthTokens? {
        return try {
            dataStore.data.map { preferences ->
                preferences[TOKENS_KEY]?.let { json.decodeFromString<AuthTokens>(it) }
            }.first()
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun clearAuthData() {
        dataStore.edit { preferences ->
            preferences.remove(USER_KEY)
            preferences.remove(TOKENS_KEY)
        }
    }
    
    override fun isLoggedIn(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[USER_KEY] != null && preferences[TOKENS_KEY] != null
        }
    }
}