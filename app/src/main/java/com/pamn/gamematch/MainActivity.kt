package com.pamn.gamematch

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pamn.gamematch.ui.auth.LoginScreen
import com.pamn.gamematch.ui.auth.SignUpScreen
import com.pamn.gamematch.ui.theme.GameMatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameMatchTheme {
                // Estado simple para alternar entre pantallas de autenticación
                var currentScreen by remember { mutableStateOf("login") }

                when (currentScreen) {
                    "login" -> LoginScreen(
                        onLoginSuccess = {
                            Toast.makeText(this, "¡Sesión Iniciada!", Toast.LENGTH_SHORT).show()
                            // Aquí cambiaremos a SwipeScreen más adelante
                        },
                        onNavigateToSignUp = { currentScreen = "signup" }
                    )
                    "signup" -> SignUpScreen(
                        onSignUpSuccess = {
                            Toast.makeText(this, "¡Usuario registrado correctamente!", Toast.LENGTH_SHORT).show()
                            currentScreen = "login"
                        },
                        onNavigateToLogin = { currentScreen = "login" }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GameMatchTheme {
        Greeting("Android")
    }
}