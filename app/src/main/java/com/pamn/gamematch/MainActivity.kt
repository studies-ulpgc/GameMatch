package com.pamn.gamematch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.pamn.gamematch.ui.MainContainerScreen
import com.pamn.gamematch.ui.auth.LoginScreen
import com.pamn.gamematch.ui.auth.OnboardingScreen
import com.pamn.gamematch.ui.auth.SignUpScreen
import com.pamn.gamematch.ui.theme.GameMatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            var currentScreen by remember { mutableStateOf("login") }

            GameMatchTheme(darkTheme = isDarkMode) {
                when (currentScreen) {
                    "login" -> LoginScreen(
                        onLoginSuccess = { currentScreen = "main" },
                        onNavigateToSignUp = { currentScreen = "signup" }
                    )
                    "signup" -> SignUpScreen(
                        onSignUpSuccess = { currentScreen = "onboarding" },
                        onNavigateToLogin = { currentScreen = "login" }
                    )
                    "onboarding" -> OnboardingScreen(
                        onOnboardingComplete = { currentScreen = "main" }
                    )
                    "main" -> MainContainerScreen(
                        onLogout = { currentScreen = "login" },
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { isDarkMode = it }
                    )
                }
            }
        }
    }
}