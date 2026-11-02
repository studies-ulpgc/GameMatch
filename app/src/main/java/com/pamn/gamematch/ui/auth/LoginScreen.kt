package com.pamn.gamematch.ui.auth

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = remember { AuthViewModel() },
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailTouched by remember { mutableStateOf(false) }
    var emailHasFocused by remember { mutableStateOf(false) }

    var passwordTouched by remember { mutableStateOf(false) }
    var passwordHasFocused by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "GameMatch", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        // Campo Correo con error al abandonar
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                viewModel.clearError()
            },
            label = { Text("Correo electrónico") },
            singleLine = true,
            isError = emailTouched && (email.isBlank() || !isEmailValid),
            supportingText = {
                if (emailTouched) {
                    when {
                        email.isBlank() -> Text("El correo electrónico es obligatorio")
                        !isEmailValid -> Text("Introduce un correo válido (ej. usuario@gmail.com)")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        emailHasFocused = true
                    } else if (emailHasFocused) {
                        emailTouched = true
                    }
                }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Campo Contraseña con error al abandonar
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.clearError()
            },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Mostrar/ocultar contraseña"
                    )
                }
            },
            isError = passwordTouched && password.isBlank(),
            supportingText = {
                if (passwordTouched && password.isBlank()) {
                    Text("La contraseña es obligatoria")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        passwordHasFocused = true
                    } else if (passwordHasFocused) {
                        passwordTouched = true
                    }
                }
        )
        Spacer(modifier = Modifier.height(16.dp))

        viewModel.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (viewModel.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    emailTouched = true
                    passwordTouched = true
                    if (email.isNotBlank() && isEmailValid && password.isNotBlank()) {
                        viewModel.login(email, password, onLoginSuccess)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar Sesión")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onNavigateToSignUp) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onLoginSuccess = {},
        onNavigateToSignUp = {}
    )
}