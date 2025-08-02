package com.jetbrains.kmpapp.screens.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.jetbrains.kmpapp.data.AuthRepository
import com.jetbrains.kmpapp.data.AuthState
import com.jetbrains.kmpapp.data.AuthUser
import com.jetbrains.kmpapp.data.LoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthScreenModel(
    private val authRepository: AuthRepository
) : ScreenModel {
    
    val authState: StateFlow<AuthState> = authRepository.authState
    
    private val _loginInProgress = MutableStateFlow(false)
    val loginInProgress: StateFlow<Boolean> = _loginInProgress.asStateFlow()
    
    private val _logoutInProgress = MutableStateFlow(false)
    val logoutInProgress: StateFlow<Boolean> = _logoutInProgress.asStateFlow()
    
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()
    
    fun login(authCode: String, redirectUri: String) {
        screenModelScope.launch {
            _loginInProgress.value = true
            _loginError.value = null
            
            val result = authRepository.login(authCode, redirectUri)
            
            if (!result.success) {
                _loginError.value = result.error
            }
            
            _loginInProgress.value = false
        }
    }
    
    fun logout() {
        screenModelScope.launch {
            _logoutInProgress.value = true
            
            authRepository.logout()
            
            _logoutInProgress.value = false
        }
    }
    
    fun clearLoginError() {
        _loginError.value = null
    }
    
    suspend fun getCurrentUser(): AuthUser? {
        return authRepository.getCurrentUser()
    }
    
    fun isLoggedIn(): kotlinx.coroutines.flow.Flow<Boolean> {
        return authRepository.isLoggedIn()
    }
}