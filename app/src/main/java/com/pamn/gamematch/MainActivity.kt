package com.pamn.gamematch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.pamn.gamematch.ui.MainContainerScreen
import com.pamn.gamematch.ui.auth.LoginScreen
import com.pamn.gamematch.ui.auth.OnboardingScreen
import com.pamn.gamematch.ui.auth.SignUpScreen
import com.pamn.gamematch.ui.settings.AppSettingsViewModel
import com.pamn.gamematch.ui.theme.GameMatchTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: AppSettingsViewModel = viewModel()
            val settingsState by settingsViewModel.uiState.collectAsState()

            val auth = FirebaseAuth.getInstance()
            val startScreen = if (auth.currentUser != null) "main" else "login"
            var currentScreen by rememberSaveable { mutableStateOf(startScreen) }

            // Cambia el color de los iconos de la barra de estado (hora/batería)
            DisposableEffect(settingsState.isDarkMode) {
                enableEdgeToEdge(
                    statusBarStyle = if (settingsState.isDarkMode) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
                onDispose { }
            }

            // Carga ajustes cuando la pantalla principal es activa
            LaunchedEffect(currentScreen) {
                if (auth.currentUser != null) {
                    settingsViewModel.loadSettings()
                }
            }

            GameMatchTheme(darkTheme = settingsState.isDarkMode) {
                when (currentScreen) {
                    "login" -> LoginScreen(
                        onLoginSuccess = {
                            currentScreen = "main"
                        },
                        onNavigateToSignUp = { currentScreen = "signup" }
                    )
                    "signup" -> SignUpScreen(
                        onSignUpSuccess = { currentScreen = "onboarding" },
                        onNavigateToLogin = { currentScreen = "login" }
                    )
                    "onboarding" -> OnboardingScreen(
                        onOnboardingComplete = { currentScreen = "main" },
                        isDarkMode = settingsState.isDarkMode,
                        onToggleDarkMode = { settingsViewModel.onToggleDarkMode(it, saveToFirestore = false) },
                        currentLanguage = settingsState.language,
                        onChangeLanguage = { settingsViewModel.onChangeLanguage(it, saveToFirestore = false) }
                    )
                    "main" -> MainContainerScreen(
                        onLogout = { currentScreen = "login" },
                        isDarkMode = settingsState.isDarkMode,
                        onToggleDarkMode = { settingsViewModel.onToggleDarkMode(it, saveToFirestore = true) },
                        currentLanguage = settingsState.language,
                        onChangeLanguage = { settingsViewModel.onChangeLanguage(it, saveToFirestore = true) }
                    )
                }
            }
        }
    }
}
