package com.pamn.gamematch.ui.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pamn.gamematch.R
import com.pamn.gamematch.ui.AppViewModelProvider
import com.pamn.gamematch.ui.theme.GameMatchTheme
import com.pamn.gamematch.ui.theme.SoftErrorColor
import com.pamn.gamematch.ui.theme.SteamAccentBlue
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
        onLogin = { email, pass -> viewModel.login(email, pass, onLoginSuccess) },
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
    var submitted by remember { mutableStateOf(false) }

    val isEmailValid = ValidationUtils.isValidEmail(email)
    val semanticColors = GameMatchTheme.semanticColors

    AuthScreenContainer(
        title = stringResource(id = R.string.app_name),
        subtitle = stringResource(id = R.string.login_subtitle)
    ) {
        AuthEmailField(
            value = email,
            onValueChange = {
                email = it
                onClearError()
            },
            isEmailValid = isEmailValid,
            invalidEmailError = stringResource(id = R.string.login_email_invalid),
            forceShowError = submitted
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthPasswordField(
            value = password,
            onValueChange = {
                password = it
                onClearError()
            },
            label = stringResource(id = R.string.login_password),
            isError = password.isBlank(),
            errorText = if (password.isBlank()) stringResource(id = R.string.login_password_required) else null,
            toggleContentDescription = stringResource(id = R.string.auth_cd_toggle_password),
            forceShowError = submitted
        )

        Spacer(modifier = Modifier.height(16.dp))

        uiState.errorMessage?.let { error ->
            Text(
                text = error.asString(),
                color = SoftErrorColor,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(color = SteamAccentBlue)
        } else {
            Button(
                onClick = {
                    submitted = true
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
