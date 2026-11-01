package com.pamn.gamematch.ui.auth

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel = remember { AuthViewModel() },
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Estados para controlar si el usuario tocó cada casilla
    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }
    var confirmTouched by remember { mutableStateOf(false) }

    // Evaluaciones de error
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isPasswordValid = password.length >= 6
    val isConfirmValid = confirmPassword.isNotEmpty() && confirmPassword == password

    val passwordStrength = viewModel.getPasswordStrength(password)
    val strengthColor = when {
        passwordStrength < 0.35f -> MaterialTheme.colorScheme.error
        passwordStrength < 0.7f -> Color(0xFFFFA000)
        else -> Color(0xFF4CAF50)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Crear Cuenta", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        // Campo Correo con validacion al perder foco
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                viewModel.clearError()
            },
            label = { Text("Correo electrónico") },
            singleLine = true,
            isError = emailTouched && !isEmailValid && email.isNotEmpty(),
            supportingText = {
                if (emailTouched && !isEmailValid && email.isNotEmpty()) {
                    Text("Formato de correo no válido (ej. usuario@gmail.com)")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && email.isNotEmpty()) emailTouched = true
                }
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Campo Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.clearError()
            },
            label = { Text("Contraseña (min. 6 caracteres)") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = passwordTouched && !isPasswordValid && password.isNotEmpty(),
            supportingText = {
                if (passwordTouched && !isPasswordValid && password.isNotEmpty()) {
                    Text("La contraseña debe tener al menos 6 caracteres")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && password.isNotEmpty()) passwordTouched = true
                }
        )

        if (password.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { passwordStrength },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = strengthColor
            )
            Text(
                text = when {
                    passwordStrength < 0.35f -> "Seguridad: Débil"
                    passwordStrength < 0.7f -> "Seguridad: Media"
                    else -> "Seguridad: Fuerte"
                },
                style = MaterialTheme.typography.labelSmall,
                color = strengthColor,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Campo Confirmar Contraseña
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                viewModel.clearError()
            },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = confirmTouched && !isConfirmValid && confirmPassword.isNotEmpty(),
            supportingText = {
                if (confirmTouched && !isConfirmValid && confirmPassword.isNotEmpty()) {
                    Text("Las contraseñas no coinciden")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && confirmPassword.isNotEmpty()) confirmTouched = true
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
                    confirmTouched = true
                    viewModel.signUp(email, password, confirmPassword, onSignUpSuccess)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrarse")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("¿Ya tienes cuenta? Inicia Sesión")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(
        onSignUpSuccess = {},
        onNavigateToLogin = {}
    )
}