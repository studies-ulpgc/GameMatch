package com.pamn.gamematch.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pamn.gamematch.BuildConfig
import com.pamn.gamematch.R
import com.pamn.gamematch.ui.AppViewModelProvider
import com.pamn.gamematch.ui.components.AvatarSelectionDialog
import com.pamn.gamematch.ui.components.ProfileAvatar
import com.pamn.gamematch.ui.theme.GameMatchTheme
import com.pamn.gamematch.utils.GenreUtils
import com.pamn.gamematch.utils.ValidationUtils
import java.util.Calendar

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    isDarkModeGlobal: Boolean = true,
    onToggleDarkModeGlobal: (Boolean) -> Unit = {},
    currentLanguage: String = "es",
    onChangeLanguage: (String) -> Unit = {},
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            Toast.makeText(context, msg.asString(context), Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    ProfileScreenContent(
        uiState = uiState,
        onLogout = { viewModel.logout(onLogout) },
        isDarkModeGlobal = isDarkModeGlobal,
        onToggleDarkModeGlobal = onToggleDarkModeGlobal,
        currentLanguage = currentLanguage,
        onChangeLanguage = onChangeLanguage,
        onToggleEditing = viewModel::toggleEditing,
        onUsernameChanged = viewModel::onUsernameChanged,
        onBirthYearChanged = viewModel::onBirthYearChanged,
        onGenreToggled = viewModel::toggleGenre,
        onPriceRangeChanged = viewModel::onPriceRangeChanged,
        onOnlyReleasedChanged = viewModel::onOnlyReleasedChanged,
        onAvatarSelected = viewModel::onAvatarSelected,
        setShowAvatarSelector = viewModel::setShowAvatarSelector,
        setShowPasswordDialog = viewModel::setShowPasswordDialog,
        setShowDeleteDialog = viewModel::setShowDeleteDialog,
        onSaveProfile = { minYear, maxYear ->
            viewModel.saveProfile(minYear, maxYear, isDarkModeGlobal, currentLanguage)
        },
        onUpdatePassword = { newPass, onSuccess ->
            viewModel.updatePassword(newPass, onSuccess)
        },
        onDeleteAccount = { viewModel.deleteAccount(onLogout) },
        onSyncIgdb = viewModel::syncCatalogFromIgdb
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,
    onLogout: () -> Unit = {},
    isDarkModeGlobal: Boolean = true,
    onToggleDarkModeGlobal: (Boolean) -> Unit = {},
    currentLanguage: String = "es",
    onChangeLanguage: (String) -> Unit = {},
    onToggleEditing: () -> Unit = {},
    onUsernameChanged: (String) -> Unit = {},
    onBirthYearChanged: (String) -> Unit = {},
    onGenreToggled: (String) -> Unit = {},
    onPriceRangeChanged: (ClosedFloatingPointRange<Float>) -> Unit = {},
    onOnlyReleasedChanged: (Boolean) -> Unit = {},
    onAvatarSelected: (String) -> Unit = {},
    setShowAvatarSelector: (Boolean) -> Unit = {},
    setShowPasswordDialog: (Boolean) -> Unit = {},
    setShowDeleteDialog: (Boolean) -> Unit = {},
    onSaveProfile: (Int, Int) -> Unit = { _, _ -> },
    onUpdatePassword: (String, () -> Unit) -> Unit = { _, _ -> },
    onDeleteAccount: () -> Unit = {},
    onSyncIgdb: () -> Unit = {}
) {
    GameMatchTheme(darkTheme = isDarkModeGlobal) {
        val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
        val minValidYear = currentYear - 120
        val maxValidYear = currentYear - 8

        val availableGenres = GenreUtils.ALL_GENRES

        var usernameTouched by remember { mutableStateOf(false) }
        var usernameHasFocused by remember { mutableStateOf(false) }
        var yearTouched by remember { mutableStateOf(false) }
        var yearHasFocused by remember { mutableStateOf(false) }
        var isUsernameFocused by remember { mutableStateOf(false) }

        var newPasswordInput by remember { mutableStateOf("") }
        var confirmPasswordInput by remember { mutableStateOf("") }

        var newPasswordTouched by remember { mutableStateOf(false) }
        var newPasswordHasFocused by remember { mutableStateOf(false) }
        var confirmPasswordTouched by remember { mutableStateOf(false) }
        var confirmPasswordHasFocused by remember { mutableStateOf(false) }

        var isPasswordVisible by remember { mutableStateOf(false) }
        var isConfirmPasswordVisible by remember { mutableStateOf(false) }

        val isYearValid = remember(uiState.birthYear) {
            ValidationUtils.isBirthYearPrefixValid(uiState.birthYear, minValidYear, maxValidYear)
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        IconButton(onClick = onToggleEditing) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.profile_edit_cd),
                                tint = if (uiState.isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        ProfileAvatar(
                            imageUrl = uiState.profilePictureUrl,
                            isEditing = uiState.isEditing,
                            onClick = { setShowAvatarSelector(true) }
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_settings),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DarkMode,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.profile_dark_mode))
                                }
                                Switch(
                                    checked = isDarkModeGlobal,
                                    onCheckedChange = { newMode ->
                                        onToggleDarkModeGlobal(newMode)
                                    }
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.profile_language))
                                }
                                FilterChip(
                                    selected = true,
                                    onClick = {
                                        val newLangCode = if (currentLanguage == "es") "en" else "es"
                                        onChangeLanguage(newLangCode)
                                    },
                                    label = { Text(if (currentLanguage == "es") "Español" else "English") },
                                    border = null
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!uiState.isEditing) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_personal_info),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                ProfileInfoRow(label = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_username), value = if (uiState.username.isBlank()) androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_unspecified) else uiState.username)
                                ProfileInfoRow(label = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_email), value = if (uiState.email.isBlank()) androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_not_available) else uiState.email)
                                ProfileInfoRow(label = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_password), value = "••••••••••••")
                                ProfileInfoRow(label = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_birthyear), value = if (uiState.birthYear.isBlank()) androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_unspecified) else uiState.birthYear)

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                                )

                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_game_prefs),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                ProfileInfoRow(label = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_price_range), value = "${uiState.priceRange.start.toInt()} € - ${uiState.priceRange.endInclusive.toInt()} €")
                                ProfileInfoRow(label = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_released_only), value = if (uiState.onlyReleased) androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_yes) else androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_no_upcoming))
                                ProfileInfoRow(label = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_genres), value = if (uiState.selectedGenres.isEmpty()) androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_all) else uiState.selectedGenres.map { GenreUtils.getLocalizedGenre(it) }.joinToString(", "))
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = uiState.username,
                            onValueChange = onUsernameChanged,
                            label = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_username_label)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isUsernameFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            singleLine = true,
                            isError = usernameTouched && uiState.username.isBlank(),
                            supportingText = {
                                if (usernameTouched && uiState.username.isBlank()) {
                                    Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_username_required))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    isUsernameFocused = focusState.isFocused
                                    if (focusState.isFocused) {
                                        usernameHasFocused = true
                                    } else if (usernameHasFocused) {
                                        usernameTouched = true
                                    }
                                }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uiState.birthYear,
                            onValueChange = onBirthYearChanged,
                            label = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_birthyear_label, minValidYear, maxValidYear)) },
                            singleLine = true,
                            isError = !isYearValid || (yearTouched && uiState.birthYear.isBlank()),
                            supportingText = {
                                if ((yearTouched || uiState.birthYear.isNotEmpty()) && !isYearValid) {
                                    Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_birthyear_out_of_range, minValidYear, maxValidYear))
                                } else if (yearTouched && uiState.birthYear.isBlank()) {
                                    Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_birthyear_required))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        yearHasFocused = true
                                    } else if (yearHasFocused) {
                                        yearTouched = true
                                    }
                                }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_price_range_format, uiState.priceRange.start.toInt(), uiState.priceRange.endInclusive.toInt()),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                RangeSlider(
                                    value = uiState.priceRange,
                                    onValueChange = onPriceRangeChanged,
                                    valueRange = 0f..100f,
                                    steps = 19
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_released_only_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (uiState.onlyReleased) androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_released_only_desc_on) else androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_released_only_desc_off),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.onlyReleased,
                                    onCheckedChange = onOnlyReleasedChanged
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_genres),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            availableGenres.forEach { genre ->
                                FilterChip(
                                    selected = uiState.selectedGenres.contains(genre),
                                    onClick = { onGenreToggled(genre) },
                                    label = { Text(GenreUtils.getLocalizedGenre(genre)) },
                                    border = null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                usernameTouched = true
                                yearTouched = true
                                onSaveProfile(minValidYear, maxValidYear)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_save_changes), style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_account_options),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { setShowPasswordDialog(true) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_change_password))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { setShowDeleteDialog(true) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_delete_account))
                            }
                        }
                    }

                    if (BuildConfig.DEBUG) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_dev_tools),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_dev_sync_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onSyncIgdb,
                                    enabled = !uiState.isSyncingIgdb,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (uiState.isSyncingIgdb) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_dev_syncing))
                                    } else {
                                        Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_dev_sync_btn))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_logout), style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (uiState.showPasswordDialog) {
            val isNewPasswordValid = newPasswordInput.length >= 6
            val isConfirmPasswordValid = confirmPasswordInput.isNotEmpty() && confirmPasswordInput == newPasswordInput

            val newPasswordStrength = ValidationUtils.calculatePasswordStrength(newPasswordInput)
            val strengthColor = when {
                newPasswordStrength < 0.35f -> MaterialTheme.colorScheme.error
                newPasswordStrength < 0.7f -> GameMatchTheme.semanticColors.passwordStrengthMedium
                else -> GameMatchTheme.semanticColors.passwordStrengthStrong
            }

            AlertDialog(
                onDismissRequest = {
                    setShowPasswordDialog(false)
                    newPasswordTouched = false
                    confirmPasswordTouched = false
                },
                title = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_change_password)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newPasswordInput,
                            onValueChange = { newPasswordInput = it },
                            label = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_new_password_label)) },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_toggle_password_cd)
                                    )
                                }
                            },
                            isError = newPasswordTouched && (newPasswordInput.isBlank() || !isNewPasswordValid),
                            supportingText = {
                                if (newPasswordTouched) {
                                    when {
                                        newPasswordInput.isBlank() -> Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.login_password_required))
                                        !isNewPasswordValid -> Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_password_min_chars))
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        newPasswordHasFocused = true
                                    } else if (newPasswordHasFocused) {
                                        newPasswordTouched = true
                                    }
                                }
                        )

                        if (newPasswordInput.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { newPasswordStrength },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = strengthColor
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = confirmPasswordInput,
                            onValueChange = { confirmPasswordInput = it },
                            label = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.signup_confirm_password)) },
                            singleLine = true,
                            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_toggle_password_cd)
                                    )
                                }
                            },
                            isError = confirmPasswordTouched && (confirmPasswordInput.isBlank() || !isConfirmPasswordValid),
                            supportingText = {
                                if (confirmPasswordTouched) {
                                    when {
                                        confirmPasswordInput.isBlank() -> Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_confirm_password_required))
                                        !isConfirmPasswordValid -> Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.signup_confirm_password_invalid))
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        confirmPasswordHasFocused = true
                                    } else if (confirmPasswordHasFocused) {
                                        confirmPasswordTouched = true
                                    }
                                }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        newPasswordTouched = true
                        confirmPasswordTouched = true

                        if (isNewPasswordValid && isConfirmPasswordValid) {
                            onUpdatePassword(newPasswordInput) {
                                newPasswordInput = ""
                                confirmPasswordInput = ""
                                newPasswordTouched = false
                                confirmPasswordTouched = false
                            }
                        }
                    }) { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_save_changes)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        setShowPasswordDialog(false)
                        newPasswordTouched = false
                        confirmPasswordTouched = false
                    }) { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.detail_cancel)) }
                }
            )
        }

        if (uiState.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { setShowDeleteDialog(false) },
                title = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_delete_account_dialog_title)) },
                text = { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_delete_account_dialog_desc)) },
                confirmButton = {
                    Button(
                        onClick = onDeleteAccount,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.profile_confirm_delete)) }
                },
                dismissButton = {
                    TextButton(onClick = { setShowDeleteDialog(false) }) { Text(androidx.compose.ui.res.stringResource(com.pamn.gamematch.R.string.detail_cancel)) }
                }
            )
        }

        if (uiState.showAvatarSelector) {
            AvatarSelectionDialog(
                onDismiss = { setShowAvatarSelector(false) },
                onAvatarSelected = onAvatarSelected
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
