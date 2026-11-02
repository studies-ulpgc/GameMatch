package com.pamn.gamematch.ui.auth

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pamn.gamematch.ui.theme.SoftErrorColor
import com.pamn.gamematch.ui.theme.SteamAccentBlue
import com.pamn.gamematch.ui.theme.SteamDarkBg

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SteamDarkBg,
                        Color(0xFF0E1015),
                        Color(0xFF050608)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E222B)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GameMatch",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = "Encuentra tu próximo juego favorito",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD1D5DB)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Campo Correo
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        viewModel.clearError()
                    },
                    label = { Text("Correo electrónico") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        errorTextColor = Color.White, // Mantiene el texto en blanco al haber error
                        focusedBorderColor = SteamAccentBlue,
                        unfocusedBorderColor = Color(0xFF3A3F4D),
                        errorBorderColor = SoftErrorColor,
                        focusedLabelColor = SteamAccentBlue,
                        unfocusedLabelColor = Color(0xFFD1D5DB),
                        errorLabelColor = SoftErrorColor,
                        focusedLeadingIconColor = SteamAccentBlue,
                        unfocusedLeadingIconColor = SteamAccentBlue,
                        errorLeadingIconColor = SoftErrorColor,
                        focusedContainerColor = Color(0xFF171A21),
                        unfocusedContainerColor = Color(0xFF171A21),
                        errorContainerColor = Color(0xFF171A21)
                    ),
                    isError = emailTouched && (email.isBlank() || !isEmailValid),
                    supportingText = {
                        if (emailTouched) {
                            when {
                                email.isBlank() -> Text("El correo es obligatorio", color = SoftErrorColor)
                                !isEmailValid -> Text("Introduce un correo válido", color = SoftErrorColor)
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

                Spacer(modifier = Modifier.height(12.dp))

                // Campo Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        viewModel.clearError()
                    },
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        errorTextColor = Color.White, // Mantiene el texto en blanco al haber error
                        focusedBorderColor = SteamAccentBlue,
                        unfocusedBorderColor = Color(0xFF3A3F4D),
                        errorBorderColor = SoftErrorColor,
                        focusedLabelColor = SteamAccentBlue,
                        unfocusedLabelColor = Color(0xFFD1D5DB),
                        errorLabelColor = SoftErrorColor,
                        focusedLeadingIconColor = SteamAccentBlue,
                        unfocusedLeadingIconColor = SteamAccentBlue,
                        errorLeadingIconColor = SoftErrorColor,
                        focusedTrailingIconColor = Color(0xFFD1D5DB),
                        unfocusedTrailingIconColor = Color(0xFFD1D5DB),
                        errorTrailingIconColor = SoftErrorColor,
                        focusedContainerColor = Color(0xFF171A21),
                        unfocusedContainerColor = Color(0xFF171A21),
                        errorContainerColor = Color(0xFF171A21)
                    ),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Mostrar contraseña"
                            )
                        }
                    },
                    isError = passwordTouched && password.isBlank(),
                    supportingText = {
                        if (passwordTouched && password.isBlank()) {
                            Text("La contraseña es obligatoria", color = SoftErrorColor)
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
                        color = SoftErrorColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = SteamAccentBlue)
                } else {
                    Button(
                        onClick = {
                            emailTouched = true
                            passwordTouched = true
                            if (email.isNotBlank() && isEmailValid && password.isNotBlank()) {
                                viewModel.login(email, password, onLoginSuccess)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SteamAccentBlue,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Iniciar Sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onNavigateToSignUp) {
                    Text("¿No tienes cuenta? Regístrate", color = SteamAccentBlue)
                }
            }
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