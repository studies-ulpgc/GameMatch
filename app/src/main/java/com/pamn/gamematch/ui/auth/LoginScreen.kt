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
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.clearError()
    }

    LoginScreenContent(
        uiState = uiState,
        onLogin = { email, pass ->
            viewModel.login(email, pass, onLoginSuccess)
        },
        onClearError = viewModel::clearError,
        onNavigateToSignUp = onNavigateToSignUp
    )
}

@Composable
fun LoginScreenContent(
    uiState: AuthUiState,
    onLogin: (String, String) -> Unit,
    onClearError: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailTouched by remember { mutableStateOf(false) }
    var emailHasFocused by remember { mutableStateOf(false) }

    var passwordTouched by remember { mutableStateOf(false) }
    var passwordHasFocused by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isEmailValid = ValidationUtils.isValidEmail(email)
    val semanticColors = GameMatchTheme.semanticColors

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
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = semanticColors.authTextPrimary
                )
                Text(
                    text = stringResource(id = R.string.login_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = semanticColors.authTextSecondary
                )

                Spacer(modifier = Modifier.height(28.dp))

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
                                !isEmailValid -> Text(stringResource(id = R.string.login_email_invalid), color = SoftErrorColor)
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
                    isError = passwordTouched && password.isBlank(),
                    supportingText = {
                        if (passwordTouched && password.isBlank()) {
                            Text(stringResource(id = R.string.login_password_required), color = SoftErrorColor)
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
                            if (email.isNotBlank() && isEmailValid && password.isNotBlank()) {
                                onLogin(email, password)
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
                        Text(stringResource(id = R.string.login_title), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onNavigateToSignUp) {
                    Text(stringResource(id = R.string.login_no_account), color = SteamAccentBlue)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreenContent(
        uiState = AuthUiState(),
        onLogin = { _, _ -> },
        onClearError = {},
        onNavigateToSignUp = {}
    )
}
