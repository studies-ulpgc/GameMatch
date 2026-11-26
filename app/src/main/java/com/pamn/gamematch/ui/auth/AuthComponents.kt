package com.pamn.gamematch.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pamn.gamematch.R
import com.pamn.gamematch.ui.theme.GameMatchTheme
import com.pamn.gamematch.ui.theme.SoftErrorColor
import com.pamn.gamematch.ui.theme.SteamAccentBlue
import com.pamn.gamematch.ui.theme.SteamDarkBg
import com.pamn.gamematch.utils.ValidationUtils

@Composable
fun authTextFieldColors(): TextFieldColors {
    val semanticColors = GameMatchTheme.semanticColors
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = semanticColors.authTextFieldText,
        unfocusedTextColor = semanticColors.authTextFieldText,
        errorTextColor = semanticColors.authTextFieldText,
        focusedBorderColor = SteamAccentBlue,
        unfocusedBorderColor = semanticColors.authTextFieldBorder,
        errorBorderColor = SoftErrorColor,
        focusedLabelColor = SteamAccentBlue,
        unfocusedLabelColor = semanticColors.authTextSecondary,
        errorLabelColor = SoftErrorColor,
        focusedLeadingIconColor = SteamAccentBlue,
        unfocusedLeadingIconColor = SteamAccentBlue,
        errorLeadingIconColor = SoftErrorColor,
        focusedTrailingIconColor = semanticColors.authTextSecondary,
        unfocusedTrailingIconColor = semanticColors.authTextSecondary,
        errorTrailingIconColor = SoftErrorColor,
        focusedContainerColor = semanticColors.authTextFieldContainer,
        unfocusedContainerColor = semanticColors.authTextFieldContainer,
        errorContainerColor = semanticColors.authTextFieldContainer
    )
}

@Composable
fun AuthScreenContainer(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
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
            colors = CardDefaults.cardColors(containerColor = semanticColors.authCardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = semanticColors.authTextPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = semanticColors.authTextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(if (subtitle != null) 28.dp else 24.dp))
                content()
            }
        }
    }
}

@Composable
fun AuthEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    isEmailValid: Boolean,
    invalidEmailError: String,
    forceShowError: Boolean
) {
    var hasFocused by remember { mutableStateOf(false) }
    var touched by remember { mutableStateOf(false) }
    val showError = (touched || forceShowError) && (value.isBlank() || !isEmailValid)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(id = R.string.login_email)) },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        singleLine = true,
        colors = authTextFieldColors(),
        isError = showError,
        supportingText = {
            if (touched || forceShowError) {
                when {
                    value.isBlank() -> Text(stringResource(id = R.string.login_email_required), color = SoftErrorColor)
                    !isEmailValid -> Text(invalidEmailError, color = SoftErrorColor)
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (focusState.isFocused) hasFocused = true else if (hasFocused) touched = true
            }
    )
}

@Composable
fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    errorText: String?,
    toggleContentDescription: String,
    forceShowError: Boolean,
    showLeadingIcon: Boolean = true,
    colors: TextFieldColors = authTextFieldColors(),
    errorTextColor: Color = SoftErrorColor
) {
    var hasFocused by remember { mutableStateOf(false) }
    var touched by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    val showError = (touched || forceShowError) && isError

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = if (showLeadingIcon) {
            { Icon(Icons.Default.Lock, contentDescription = null) }
        } else null,
        singleLine = true,
        colors = colors,
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { isVisible = !isVisible }) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = toggleContentDescription
                )
            }
        },
        isError = showError,
        supportingText = {
            if (showError && errorText != null) {
                Text(errorText, color = errorTextColor)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (focusState.isFocused) hasFocused = true else if (hasFocused) touched = true
            }
    )
}

@Composable
fun PasswordStrengthIndicator(
    password: String,
    weakColor: Color,
    mediumColor: Color,
    strongColor: Color
) {
    if (password.isEmpty()) return
    val strength = ValidationUtils.calculatePasswordStrength(password)
    val color = when {
        strength < 0.35f -> weakColor
        strength < 0.7f -> mediumColor
        else -> strongColor
    }
    Spacer(modifier = Modifier.height(4.dp))
    LinearProgressIndicator(
        progress = { strength },
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp),
        color = color
    )
}
