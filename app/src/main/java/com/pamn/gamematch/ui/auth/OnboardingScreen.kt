package com.pamn.gamematch.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pamn.gamematch.ui.AppViewModelProvider
import com.pamn.gamematch.ui.components.AvatarSelectionDialog
import com.pamn.gamematch.ui.components.ProfileAvatar
import com.pamn.gamematch.ui.theme.GameMatchTheme
import com.pamn.gamematch.utils.GenreUtils
import com.pamn.gamematch.utils.ValidationUtils
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    isDarkMode: Boolean = true,
    onToggleDarkMode: (Boolean) -> Unit = {},
    currentLanguage: String = "es",
    onChangeLanguage: (String) -> Unit = {},
    viewModel: OnboardingViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            Toast.makeText(context, msg.asString(context), Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    OnboardingScreenContent(
        uiState = uiState,
        isDarkMode = isDarkMode,
        onToggleDarkMode = onToggleDarkMode,
        currentLanguage = currentLanguage,
        onChangeLanguage = onChangeLanguage,
        onUsernameChanged = viewModel::onUsernameChanged,
        onBirthYearChanged = viewModel::onBirthYearChanged,
        onGenreToggled = viewModel::toggleGenre,
        onPriceRangeChanged = viewModel::onPriceRangeChanged,
        onOnlyReleasedChanged = viewModel::onOnlyReleasedChanged,
        onAvatarSelected = viewModel::onAvatarSelected,
        setShowAvatarSelector = viewModel::setShowAvatarSelector,
        onComplete = { minYear, maxYear ->
            viewModel.completeOnboarding(
                minValidYear = minYear,
                maxValidYear = maxYear,
                isDarkMode = isDarkMode,
                currentLanguage = currentLanguage,
                onComplete = onOnboardingComplete
            )
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreenContent(
    uiState: OnboardingUiState,
    isDarkMode: Boolean = true,
    onToggleDarkMode: (Boolean) -> Unit = {},
    currentLanguage: String = "es",
    onChangeLanguage: (String) -> Unit = {},
    onUsernameChanged: (String) -> Unit = {},
    onBirthYearChanged: (String) -> Unit = {},
    onGenreToggled: (String) -> Unit = {},
    onPriceRangeChanged: (ClosedFloatingPointRange<Float>) -> Unit = {},
    onOnlyReleasedChanged: (Boolean) -> Unit = {},
    onAvatarSelected: (String) -> Unit = {},
    setShowAvatarSelector: (Boolean) -> Unit = {},
    onComplete: (Int, Int) -> Unit = { _, _ -> }
) {
    GameMatchTheme(darkTheme = isDarkMode) {
        val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
        val minValidYear = currentYear - 120
        val maxValidYear = currentYear - 8

        var isUsernameFocused by remember { mutableStateOf(false) }
        var usernameTouched by remember { mutableStateOf(false) }
        var usernameHasFocused by remember { mutableStateOf(false) }

        var yearTouched by remember { mutableStateOf(false) }
        var yearHasFocused by remember { mutableStateOf(false) }

        val isYearValid = remember(uiState.birthYear) {
            ValidationUtils.isBirthYearPrefixValid(uiState.birthYear, minValidYear, maxValidYear)
        }
        val availableGenres = GenreUtils.ALL_GENRES

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProfileAvatar(
                    imageUrl = uiState.profilePictureUrl,
                    isEditing = true,
                    onClick = { setShowAvatarSelector(true) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_appearance),
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
                                Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_dark_mode))
                            }
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { onToggleDarkMode(it) }
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
                                Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_language))
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

                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = onUsernameChanged,
                    label = { Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_username_label)) },
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
                            Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_username_error))
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
                    label = { Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_birthyear_label)) },
                    singleLine = true,
                    isError = !isYearValid || (yearTouched && uiState.birthYear.isBlank()),
                    supportingText = {
                        if ((yearTouched || uiState.birthYear.isNotEmpty()) && !isYearValid) {
                            Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_birthyear_error_out_of_range) + " ($minValidYear - $maxValidYear)")
                        } else if (yearTouched && uiState.birthYear.isBlank()) {
                            Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_birthyear_error_required))
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
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_price_range) + " ${uiState.priceRange.start.toInt()} € - ${uiState.priceRange.endInclusive.toInt()} €",
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
                                text = androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_released_only_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (uiState.onlyReleased) androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_released_only_desc_on) else androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_released_only_desc_off),
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

                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_genres_title),
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
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        usernameTouched = true
                        yearTouched = true
                        onComplete(minValidYear, maxValidYear)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(androidx.compose.ui.res.stringResource(id = com.pamn.gamematch.R.string.onboarding_continue), style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        if (uiState.showAvatarSelector) {
            AvatarSelectionDialog(
                onDismiss = { setShowAvatarSelector(false) },
                onAvatarSelected = onAvatarSelected
            )
        }
    }
}
