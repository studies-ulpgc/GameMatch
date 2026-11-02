package com.pamn.gamematch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pamn.gamematch.ui.MainContainerScreen
import com.pamn.gamematch.ui.auth.LoginScreen
import com.pamn.gamematch.ui.auth.OnboardingScreen
import com.pamn.gamematch.ui.auth.SignUpScreen
import com.pamn.gamematch.ui.theme.GameMatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var isDarkMode by remember { mutableStateOf(true) } // Cambiado a true por defecto
            var currentScreen by remember { mutableStateOf("login") }

            // Cambia el color de los iconos de la barra de estado (hora/batería)
            DisposableEffect(isDarkMode) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDarkMode) {
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

            // Carga el tema guardado en Firebase al iniciar
            LaunchedEffect(currentScreen) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    FirebaseFirestore.getInstance().collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            doc.getBoolean("isDarkMode")?.let { savedMode ->
                                isDarkMode = savedMode
                            }
                        }
                }
            }

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
                        onOnboardingComplete = { currentScreen = "main" },
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { isDarkMode = it }
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