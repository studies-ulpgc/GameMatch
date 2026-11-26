package com.pamn.gamematch.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import com.pamn.gamematch.R
import com.pamn.gamematch.ui.AppViewModelProvider
import com.pamn.gamematch.ui.components.AppearanceSettingsCard
import com.pamn.gamematch.ui.components.AvatarSelectionDialog
import com.pamn.gamematch.ui.components.BirthYearField
import com.pamn.gamematch.ui.components.GenreSelectorSection
import com.pamn.gamematch.ui.components.OnlyReleasedCard
import com.pamn.gamematch.ui.components.PriceRangeCard
import com.pamn.gamematch.ui.components.ProfileAvatar
import com.pamn.gamematch.ui.components.UsernameField
import com.pamn.gamematch.ui.theme.GameMatchTheme
import com.pamn.gamematch.utils.GenreUtils
import com.pamn.gamematch.utils.ValidationUtils
import java.util.Calendar

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
        var submitted by remember { mutableStateOf(false) }

        val isYearValid = remember(uiState.birthYear) {
            ValidationUtils.isBirthYearPrefixValid(uiState.birthYear, minValidYear, maxValidYear)
        }

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
                    text = stringResource(R.string.onboarding_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.onboarding_subtitle),
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
                AppearanceSettingsCard(
                    title = stringResource(R.string.onboarding_appearance),
                    darkModeLabel = stringResource(R.string.onboarding_dark_mode),
                    languageLabel = stringResource(R.string.onboarding_language),
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode,
                    currentLanguage = currentLanguage,
                    onChangeLanguage = onChangeLanguage
                )

                Spacer(modifier = Modifier.height(20.dp))
                UsernameField(
                    value = uiState.username,
                    onValueChange = onUsernameChanged,
                    label = stringResource(R.string.onboarding_username_label),
                    requiredError = stringResource(R.string.onboarding_username_error),
                    forceShowError = submitted
                )

                Spacer(modifier = Modifier.height(12.dp))
                BirthYearField(
                    value = uiState.birthYear,
                    onValueChange = onBirthYearChanged,
                    label = stringResource(R.string.onboarding_birthyear_label),
                    isYearValid = isYearValid,
                    outOfRangeError = stringResource(R.string.onboarding_birthyear_error_out_of_range) + " ($minValidYear - $maxValidYear)",
                    requiredError = stringResource(R.string.onboarding_birthyear_error_required),
                    forceShowError = submitted
                )

                Spacer(modifier = Modifier.height(20.dp))
                PriceRangeCard(
                    titleText = stringResource(R.string.onboarding_price_range) + " ${uiState.priceRange.start.toInt()} € - ${uiState.priceRange.endInclusive.toInt()} €",
                    priceRange = uiState.priceRange,
                    onPriceRangeChanged = onPriceRangeChanged
                )

                Spacer(modifier = Modifier.height(20.dp))
                OnlyReleasedCard(
                    title = stringResource(R.string.onboarding_released_only_title),
                    description = if (uiState.onlyReleased) {
                        stringResource(R.string.onboarding_released_only_desc_on)
                    } else {
                        stringResource(R.string.onboarding_released_only_desc_off)
                    },
                    onlyReleased = uiState.onlyReleased,
                    onOnlyReleasedChanged = onOnlyReleasedChanged
                )

                Spacer(modifier = Modifier.height(20.dp))
                GenreSelectorSection(
                    title = stringResource(R.string.onboarding_genres_title),
                    availableGenres = GenreUtils.ALL_GENRES,
                    selectedGenres = uiState.selectedGenres,
                    onGenreToggled = onGenreToggled
                )

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        submitted = true
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
                        Text(stringResource(R.string.onboarding_continue), style = MaterialTheme.typography.titleMedium)
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
