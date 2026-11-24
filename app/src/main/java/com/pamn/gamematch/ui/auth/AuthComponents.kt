package com.pamn.gamematch.ui.auth

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import com.pamn.gamematch.ui.theme.GameMatchTheme
import com.pamn.gamematch.ui.theme.SoftErrorColor
import com.pamn.gamematch.ui.theme.SteamAccentBlue

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
