package com.pamn.gamematch.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged

@Composable
fun UsernameField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    requiredError: String,
    forceShowError: Boolean,
    modifier: Modifier = Modifier
) {
    var hasFocused by remember { mutableStateOf(false) }
    var touched by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val showError = (touched || forceShowError) && value.isBlank()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = true,
        isError = showError,
        supportingText = {
            if (showError) {
                Text(requiredError)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    hasFocused = true
                } else if (hasFocused) {
                    touched = true
                }
            }
    )
}

@Composable
fun BirthYearField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isYearValid: Boolean,
    outOfRangeError: String,
    requiredError: String,
    forceShowError: Boolean,
    modifier: Modifier = Modifier
) {
    var hasFocused by remember { mutableStateOf(false) }
    var touched by remember { mutableStateOf(false) }
    val showOutOfRange = (touched || forceShowError || value.isNotEmpty()) && !isYearValid
    val showRequired = (touched || forceShowError) && value.isBlank()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = !isYearValid || showRequired,
        supportingText = {
            when {
                showOutOfRange -> Text(outOfRangeError)
                showRequired -> Text(requiredError)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    hasFocused = true
                } else if (hasFocused) {
                    touched = true
                }
            }
    )
}
