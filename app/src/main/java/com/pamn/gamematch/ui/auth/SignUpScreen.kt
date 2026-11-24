package com.pamn.gamematch.ui.auth

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pamn.gamematch.R
import com.pamn.gamematch.ui.AppViewModelProvider
import com.pamn.gamematch.ui.theme.GameMatchTheme
import com.pamn.gamematch.ui.theme.SoftErrorColor
import com.pamn.gamematch.ui.theme.SteamAccentBlue
import com.pamn.gamematch.ui.theme.SteamDarkBg
import com.pamn.gamematch.utils.ValidationUtils

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.clearError()
    }

    SignUpScreenContent(
        uiState = uiState,
        onSignUp = { email, pass, confirmPass ->
            viewModel.signUp(email, pass, confirmPass, onSignUpSuccess)
        },
        onClearError = viewModel::clearError,
        onNavigateToLogin = onNavigateToLogin
    )
}

@Composable
fun SignUpScreenContent(
    uiState: AuthUiState,
    onSignUp: (String, String, String) -> Unit,
    onClearError: () -> Unit,
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

    val isEmailValid = ValidationUtils.isValidEmail(email)
    val isPasswordValid = password.length >= 6
    val isConfirmValid = confirmPassword.isNotEmpty() && confirmPassword == password

    val passwordStrength = ValidationUtils.calculatePasswordStrength(password)
    val semanticColors = GameMatchTheme.semanticColors
    val strengthColor = when {
        passwordStrength < 0.35f -> semanticColors.passwordStrengthWeak
        passwordStrength < 0.7f -> semanticColors.passwordStrengthMedium
        else -> semanticColors.passwordStrengthStrong
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SteamDarkBg,
                        semanticColors.authGradientMid,
                        semanticColors.authGradientBottom
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
                containerColor = semanticColors.authCardBg
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
                    text = stringResource(id = R.string.signup_title),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = semanticColors.authTextPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Campo Correo
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        onClearError()
                    },
                    label = { Text(stringResource(id = R.string.login_email)) },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    singleLine = true,
                    colors = authTextFieldColors(),
                    isError = emailTouched && (email.isBlank() || !isEmailValid),
                    supportingText = {
                        if (emailTouched) {
                            when {
                                email.isBlank() -> Text(stringResource(id = R.string.login_email_required), color = SoftErrorColor)
                                !isEmailValid -> Text(stringResource(id = R.string.signup_email_invalid), color = SoftErrorColor)
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
                        onClearError()
                    },
                    label = { Text(stringResource(id = R.string.login_password)) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    singleLine = true,
                    colors = authTextFieldColors(),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = stringResource(id = R.string.auth_cd_toggle_password)
                            )
                        }
                    },
                    isError = passwordTouched && (password.isBlank() || !isPasswordValid),
                    supportingText = {
                        if (passwordTouched) {
                            when {
                                password.isBlank() -> Text(stringResource(id = R.string.login_password_required), color = SoftErrorColor)
                                !isPasswordValid -> Text(stringResource(id = R.string.signup_password_invalid), color = SoftErrorColor)
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
                        onClearError()
                    },
                    label = { Text(stringResource(id = R.string.signup_confirm_password)) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    singleLine = true,
                    colors = authTextFieldColors(),
                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                            Icon(
                                imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = stringResource(id = R.string.auth_cd_toggle_password)
                            )
                        }
                    },
                    isError = confirmTouched && (confirmPassword.isBlank() || !isConfirmValid),
                    supportingText = {
                        if (confirmTouched) {
                            when {
                                confirmPassword.isBlank() -> Text(stringResource(id = R.string.signup_confirm_password), color = SoftErrorColor)
                                !isConfirmValid -> Text(stringResource(id = R.string.signup_confirm_password_invalid), color = SoftErrorColor)
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

                uiState.errorMessage?.let { error ->
                    Text(
                        text = error.asString(),
                        color = SoftErrorColor,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator(color = SteamAccentBlue)
                } else {
                    Button(
                        onClick = {
                            emailTouched = true
                            passwordTouched = true
                            confirmTouched = true
                            if (isEmailValid && isPasswordValid && isConfirmValid) {
                                onSignUp(email, password, confirmPassword)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SteamAccentBlue,
                            contentColor = semanticColors.authButtonText
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(stringResource(id = R.string.signup_title), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onNavigateToLogin) {
                    Text(stringResource(id = R.string.signup_already_account), color = SteamAccentBlue)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreenContent(
        uiState = AuthUiState(),
        onSignUp = { _, _, _ -> },
        onClearError = {},
        onNavigateToLogin = {}
    )
}
