package com.pamn.gamematch.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.R
import com.pamn.gamematch.ui.auth.AuthPasswordField
import com.pamn.gamematch.ui.auth.PasswordStrengthIndicator
import com.pamn.gamematch.ui.theme.GameMatchTheme

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onUpdatePassword: (String, () -> Unit) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    val isNewValid = newPassword.length >= 6
    val isConfirmValid = confirmPassword.isNotEmpty() && confirmPassword == newPassword
    val fieldColors = OutlinedTextFieldDefaults.colors()
    val errorColor = MaterialTheme.colorScheme.error

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_change_password)) },
        text = {
            Column {
                AuthPasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = stringResource(R.string.profile_new_password_label),
                    isError = newPassword.isBlank() || !isNewValid,
                    errorText = when {
                        newPassword.isBlank() -> stringResource(R.string.login_password_required)
                        !isNewValid -> stringResource(R.string.profile_password_min_chars)
                        else -> null
                    },
                    toggleContentDescription = stringResource(R.string.profile_toggle_password_cd),
                    forceShowError = submitted,
                    showLeadingIcon = false,
                    colors = fieldColors,
                    errorTextColor = errorColor
                )

                PasswordStrengthIndicator(
                    password = newPassword,
                    weakColor = errorColor,
                    mediumColor = GameMatchTheme.semanticColors.passwordStrengthMedium,
                    strongColor = GameMatchTheme.semanticColors.passwordStrengthStrong
                )

                Spacer(modifier = Modifier.height(12.dp))

                AuthPasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = stringResource(R.string.signup_confirm_password),
                    isError = confirmPassword.isBlank() || !isConfirmValid,
                    errorText = when {
                        confirmPassword.isBlank() -> stringResource(R.string.profile_confirm_password_required)
                        !isConfirmValid -> stringResource(R.string.signup_confirm_password_invalid)
                        else -> null
                    },
                    toggleContentDescription = stringResource(R.string.profile_toggle_password_cd),
                    forceShowError = submitted,
                    showLeadingIcon = false,
                    colors = fieldColors,
                    errorTextColor = errorColor
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                submitted = true
                if (isNewValid && isConfirmValid) {
                    onUpdatePassword(newPassword) {
                        newPassword = ""
                        confirmPassword = ""
                        submitted = false
                    }
                }
            }) {
                Text(stringResource(R.string.profile_save_changes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.detail_cancel))
            }
        }
    )
}

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_delete_account_dialog_title)) },
        text = { Text(stringResource(R.string.profile_delete_account_dialog_desc)) },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.profile_confirm_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.detail_cancel))
            }
        }
    )
}
