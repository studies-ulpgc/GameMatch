package com.pamn.gamematch.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pamn.gamematch.BuildConfig
import com.pamn.gamematch.R
import com.pamn.gamematch.ui.AppViewModelProvider
import com.pamn.gamematch.ui.components.AppearanceSettingsCard
import com.pamn.gamematch.ui.components.AvatarSelectionDialog
import com.pamn.gamematch.ui.components.ProfileAvatar
import com.pamn.gamematch.ui.theme.GameMatchTheme
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

    LaunchedEffect(Unit) { viewModel.loadProfile() }

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
        onUpdatePassword = viewModel::updatePassword,
        onDeleteAccount = { viewModel.deleteAccount(onLogout) },
        onSyncIgdb = viewModel::syncCatalogFromIgdb
    )
}

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
        var submitted by remember { mutableStateOf(false) }

        val isYearValid = remember(uiState.birthYear) {
            ValidationUtils.isBirthYearPrefixValid(uiState.birthYear, minValidYear, maxValidYear)
        }

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
                            text = stringResource(R.string.profile_title),
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
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        ProfileAvatar(
                            imageUrl = uiState.profilePictureUrl,
                            isEditing = uiState.isEditing,
                            onClick = { setShowAvatarSelector(true) }
                        )
                    }

                    AppearanceSettingsCard(
                        title = stringResource(R.string.profile_settings),
                        darkModeLabel = stringResource(R.string.profile_dark_mode),
                        languageLabel = stringResource(R.string.profile_language),
                        isDarkMode = isDarkModeGlobal,
                        onToggleDarkMode = onToggleDarkModeGlobal,
                        currentLanguage = currentLanguage,
                        onChangeLanguage = onChangeLanguage
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!uiState.isEditing) {
                        ProfileReadOnlyCard(uiState = uiState)
                    } else {
                        ProfileEditSection(
                            uiState = uiState,
                            minValidYear = minValidYear,
                            maxValidYear = maxValidYear,
                            isYearValid = isYearValid,
                            forceShowError = submitted,
                            onUsernameChanged = onUsernameChanged,
                            onBirthYearChanged = onBirthYearChanged,
                            onPriceRangeChanged = onPriceRangeChanged,
                            onOnlyReleasedChanged = onOnlyReleasedChanged,
                            onGenreToggled = onGenreToggled,
                            onSave = {
                                submitted = true
                                onSaveProfile(minValidYear, maxValidYear)
                            },
                            onOpenPasswordDialog = { setShowPasswordDialog(true) },
                            onOpenDeleteDialog = { setShowDeleteDialog(true) }
                        )
                    }

                    if (BuildConfig.DEBUG) {
                        Spacer(modifier = Modifier.height(20.dp))
                        ProfileDevToolsCard(isSyncingIgdb = uiState.isSyncingIgdb, onSyncIgdb = onSyncIgdb)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text(stringResource(R.string.profile_logout), style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (uiState.showPasswordDialog) {
            ChangePasswordDialog(onDismiss = { setShowPasswordDialog(false) }, onUpdatePassword = onUpdatePassword)
        }
        if (uiState.showDeleteDialog) {
            DeleteAccountDialog(onDismiss = { setShowDeleteDialog(false) }, onConfirmDelete = onDeleteAccount)
        }
        if (uiState.showAvatarSelector) {
            AvatarSelectionDialog(onDismiss = { setShowAvatarSelector(false) }, onAvatarSelected = onAvatarSelected)
        }
    }
}
