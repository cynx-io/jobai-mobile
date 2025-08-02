package com.jetbrains.kmpapp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.koin.getScreenModel
import com.jetbrains.kmpapp.data.AuthState
import com.jetbrains.kmpapp.screens.auth.AuthScreenModel
import com.jetbrains.kmpapp.screens.auth.LoginScreen
import com.jetbrains.kmpapp.screens.list.ListScreen

@Composable
fun App() {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    ) {
        Surface {
            AuthGuard()
        }
    }
}

@Composable
private fun AuthGuard() {
    val authScreenModel: AuthScreenModel = getScreenModel()
    val authState by authScreenModel.authState.collectAsStateWithLifecycle()
    
    when (authState) {
        is AuthState.Loading -> {
            // Show loading indicator while checking auth state
            Navigator(ListScreen) // For now, show main app during loading
        }
        is AuthState.Authenticated -> {
            // User is authenticated, show main app
            Navigator(ListScreen)
        }
        is AuthState.Unauthenticated -> {
            // User is not authenticated, show login screen
            Navigator(LoginScreen)
        }
        is AuthState.Error -> {
            // Error in auth, show login screen
            Navigator(LoginScreen)
        }
    }
}
