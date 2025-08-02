package com.jetbrains.kmpapp.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.header
import kotlinx.coroutines.CancellationException

interface AuthApi {
    suspend fun login(authCode: String, redirectUri: String): AuthResponse
    suspend fun logout(accessToken: String): Boolean
    suspend fun refreshToken(refreshToken: String): AuthTokens
}

class KtorAuthApi(private val client: HttpClient) : AuthApi {
    
    override suspend fun login(authCode: String, redirectUri: String): AuthResponse {
        return try {
            val url = "${Config.API_BASE_URL}${Config.AUTH_LOGIN_ENDPOINT}?code=$authCode&redirect_uri=$redirectUri"
            client.post(url).body()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            throw Exception("Login failed: ${e.message}")
        }
    }
    
    override suspend fun logout(accessToken: String): Boolean {
        return try {
            val url = "${Config.API_BASE_URL}${Config.AUTH_LOGOUT_ENDPOINT}"
            client.post(url) {
                header("Authorization", "Bearer $accessToken")
            }
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            false
        }
    }
    
    override suspend fun refreshToken(refreshToken: String): AuthTokens {
        return try {
            val url = "${Config.API_BASE_URL}/auth0/refresh"
            client.post(url) {
                header("Authorization", "Bearer $refreshToken")
            }.body()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            throw Exception("Token refresh failed: ${e.message}")
        }
    }
}