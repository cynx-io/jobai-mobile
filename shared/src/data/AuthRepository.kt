package com.jetbrains.kmpapp.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AuthRepository(
    private val authApi: AuthApi,
    private val authStorage: AuthStorage
) {
    private val scope = CoroutineScope(SupervisorJob())
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        initialize()
    }
    
    fun initialize() {
        scope.launch {
            try {
                val user = authStorage.getAuthUser()
                val tokens = authStorage.getAuthTokens()
                
                if (user != null && tokens != null) {
                    if (isTokenValid(tokens)) {
                        _authState.value = AuthState.Authenticated(user)
                    } else {
                        // Try to refresh token
                        tokens.refreshToken?.let { refreshToken ->
                            try {
                                val newTokens = authApi.refreshToken(refreshToken)
                                authStorage.saveAuthData(user, newTokens)
                                _authState.value = AuthState.Authenticated(user)
                            } catch (e: Exception) {
                                // Refresh failed, clear auth data
                                clearAuthData()
                                _authState.value = AuthState.Unauthenticated
                            }
                        } ?: run {
                            // No refresh token, clear auth data
                            clearAuthData()
                            _authState.value = AuthState.Unauthenticated
                        }
                    }
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Failed to initialize auth: ${e.message}")
            }
        }
    }
    
    suspend fun login(authCode: String, redirectUri: String): LoginResult {
        _authState.value = AuthState.Loading
        
        return try {
            val authResponse = authApi.login(authCode, redirectUri)
            
            // Save auth data
            authStorage.saveAuthData(authResponse.user, authResponse.tokens)
            
            _authState.value = AuthState.Authenticated(authResponse.user)
            
            LoginResult(
                success = true,
                authResponse = authResponse
            )
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Login failed: ${e.message}")
            LoginResult(
                success = false,
                error = e.message
            )
        }
    }
    
    suspend fun logout(): Boolean {
        _authState.value = AuthState.Loading
        
        return try {
            val tokens = authStorage.getAuthTokens()
            var apiLogoutSuccess = true
            
            // Try to logout from API if we have tokens
            tokens?.let {
                apiLogoutSuccess = authApi.logout(it.accessToken)
            }
            
            // Clear local auth data regardless of API response
            clearAuthData()
            _authState.value = AuthState.Unauthenticated
            
            apiLogoutSuccess
        } catch (e: Exception) {
            // Clear local data even if API call fails
            clearAuthData()
            _authState.value = AuthState.Unauthenticated
            false
        }
    }
    
    suspend fun refreshAccessToken(): Boolean {
        return try {
            val currentTokens = authStorage.getAuthTokens()
            val currentUser = authStorage.getAuthUser()
            
            if (currentTokens?.refreshToken != null && currentUser != null) {
                val newTokens = authApi.refreshToken(currentTokens.refreshToken)
                authStorage.saveAuthData(currentUser, newTokens)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getCurrentUser(): AuthUser? {
        return authStorage.getAuthUser()
    }
    
    suspend fun getCurrentTokens(): AuthTokens? {
        return authStorage.getAuthTokens()
    }
    
    fun isLoggedIn(): Flow<Boolean> {
        return authStorage.isLoggedIn()
    }
    
    private suspend fun clearAuthData() {
        authStorage.clearAuthData()
    }
    
    private fun isTokenValid(tokens: AuthTokens): Boolean {
        val currentTime = System.currentTimeMillis() / 1000
        return currentTime < tokens.expiresIn
    }
}