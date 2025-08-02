package com.jetbrains.kmpapp.data

import kotlinx.serialization.Serializable

@Serializable
data class AuthUser(
    val id: String,
    val email: String,
    val name: String,
    val picture: String? = null
)

@Serializable
data class AuthTokens(
    val accessToken: String,
    val idToken: String,
    val refreshToken: String? = null,
    val expiresIn: Long
)

@Serializable
data class AuthResponse(
    val user: AuthUser,
    val tokens: AuthTokens
)

sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val user: AuthUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

data class LoginResult(
    val success: Boolean,
    val authResponse: AuthResponse? = null,
    val error: String? = null
)