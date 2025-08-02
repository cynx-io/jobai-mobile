package com.jetbrains.kmpapp.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jetbrains.kmpapp.data.AuthState
import com.jetbrains.kmpapp.screens.list.ListScreen

data object LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel: AuthScreenModel = getScreenModel()
        
        val authState by screenModel.authState.collectAsStateWithLifecycle()
        val loginInProgress by screenModel.loginInProgress.collectAsStateWithLifecycle()
        val loginError by screenModel.loginError.collectAsStateWithLifecycle()
        
        // Navigate to main screen if authenticated
        when (authState) {
            is AuthState.Authenticated -> {
                navigator.replaceAll(ListScreen)
                return
            }
            else -> {
                // Continue with login screen
            }
        }
        
        LoginContent(
            loginInProgress = loginInProgress,
            loginError = loginError,
            onLogin = { authCode, redirectUri ->
                screenModel.login(authCode, redirectUri)
            },
            onClearError = {
                screenModel.clearLoginError()
            }
        )
    }
}

@Composable
private fun LoginContent(
    loginInProgress: Boolean,
    loginError: String?,
    onLogin: (String, String) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var authCode by remember { mutableStateOf("") }
    var redirectUri by remember { mutableStateOf("") }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(WindowInsets.safeDrawing.asPaddingValues()),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Please enter your Auth0 authorization code",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = authCode,
                    onValueChange = { 
                        authCode = it
                        if (loginError != null) onClearError()
                    },
                    label = { Text("Authorization Code") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loginInProgress,
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = redirectUri,
                    onValueChange = { 
                        redirectUri = it
                        if (loginError != null) onClearError()
                    },
                    label = { Text("Redirect URI") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loginInProgress,
                    singleLine = true,
                    placeholder = { Text("e.g., your-app://callback") }
                )
                
                if (loginError != null) {
                    Text(
                        text = loginError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Button(
                    onClick = {
                        if (authCode.isNotBlank() && redirectUri.isNotBlank()) {
                            onLogin(authCode, redirectUri)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loginInProgress && authCode.isNotBlank() && redirectUri.isNotBlank()
                ) {
                    if (loginInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(16.dp)
                                .height(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Sign In")
                    }
                }
                
                Text(
                    text = "Note: This is a development interface. In production, Auth0 authentication would be handled through proper OAuth flows.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}