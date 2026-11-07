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
fun SignUpScreen(
    viewModel: AuthViewModel = remember { AuthViewModel() },
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }
    var confirmTouched by remember { mutableStateOf(false) }

    var emailHasFocused by remember { mutableStateOf(false) }
    var passwordHasFocused by remember { mutableStateOf(false) }
    var confirmHasFocused by remember { mutableStateOf(false) }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isPasswordValid = password.length >= 6
    val isConfirmValid = confirmPassword.isNotEmpty() && confirmPassword == password

    val passwordStrength = viewModel.getPasswordStrength(password)
    val strengthColor = when {
        passwordStrength < 0.35f -> SoftErrorColor
        passwordStrength < 0.7f -> Color(0xFFFFA000)
        else -> Color(0xFF4CAF50)
    }

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
                    text = androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.signup_title),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Campo Correo
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        viewModel.clearError()
                    },
                    label = { Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.login_email)) },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        errorTextColor = Color.White, // Texto blanco al fallar
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
                                email.isBlank() -> Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.login_email_required), color = SoftErrorColor)
                                !isEmailValid -> Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.signup_email_invalid), color = SoftErrorColor)
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

                Spacer(modifier = Modifier.height(8.dp))

                // Campo Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        viewModel.clearError()
                    },
                    label = { Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.login_password)) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        errorTextColor = Color.White, // Texto blanco al fallar
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
                    isError = passwordTouched && (password.isBlank() || !isPasswordValid),
                    supportingText = {
                        if (passwordTouched) {
                            when {
                                password.isBlank() -> Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.login_password_required), color = SoftErrorColor)
                                !isPasswordValid -> Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.signup_password_invalid), color = SoftErrorColor)
                            }
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

                if (password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { passwordStrength },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = strengthColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Campo Confirmar Contraseña
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        viewModel.clearError()
                    },
                    label = { Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.signup_confirm_password)) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        errorTextColor = Color.White, // Texto blanco al fallar
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
                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                            Icon(
                                imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Mostrar contraseña"
                            )
                        }
                    },
                    isError = confirmTouched && (confirmPassword.isBlank() || !isConfirmValid),
                    supportingText = {
                        if (confirmTouched) {
                            when {
                                confirmPassword.isBlank() -> Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.signup_confirm_password), color = SoftErrorColor)
                                !isConfirmValid -> Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.signup_confirm_password_invalid), color = SoftErrorColor)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                confirmHasFocused = true
                            } else if (confirmHasFocused) {
                                confirmTouched = true
                            }
                        }
                )

                Spacer(modifier = Modifier.height(16.dp))

                viewModel.errorMessage?.let { error ->
                    Text(
                        text = error.asString(),
                        color = SoftErrorColor,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                            confirmTouched = true
                            if (isEmailValid && isPasswordValid && isConfirmValid) {
                                viewModel.signUp(email, password, confirmPassword, onSignUpSuccess)
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
                        Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.signup_title), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onNavigateToLogin) {
                    Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.signup_already_account), color = SteamAccentBlue)
                }
            }
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