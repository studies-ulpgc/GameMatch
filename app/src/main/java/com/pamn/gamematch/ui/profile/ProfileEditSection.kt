package com.pamn.gamematch.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.R
import com.pamn.gamematch.ui.components.BirthYearField
import com.pamn.gamematch.ui.components.GenreSelectorSection
import com.pamn.gamematch.ui.components.OnlyReleasedCard
import com.pamn.gamematch.ui.components.PriceRangeCard
import com.pamn.gamematch.ui.components.UsernameField
import com.pamn.gamematch.utils.GenreUtils

@Composable
internal fun ProfileEditSection(
    uiState: ProfileUiState,
    minValidYear: Int,
    maxValidYear: Int,
    isYearValid: Boolean,
    forceShowError: Boolean,
    onUsernameChanged: (String) -> Unit,
    onBirthYearChanged: (String) -> Unit,
    onPriceRangeChanged: (ClosedFloatingPointRange<Float>) -> Unit,
    onOnlyReleasedChanged: (Boolean) -> Unit,
    onGenreToggled: (String) -> Unit,
    onSave: () -> Unit,
    onOpenPasswordDialog: () -> Unit,
    onOpenDeleteDialog: () -> Unit
) {
    UsernameField(
        value = uiState.username,
        onValueChange = onUsernameChanged,
        label = stringResource(R.string.profile_username_label),
        requiredError = stringResource(R.string.profile_username_required),
        forceShowError = forceShowError
    )
    Spacer(modifier = Modifier.height(12.dp))
    BirthYearField(
        value = uiState.birthYear,
        onValueChange = onBirthYearChanged,
        label = stringResource(R.string.profile_birthyear_label, minValidYear, maxValidYear),
        isYearValid = isYearValid,
        outOfRangeError = stringResource(R.string.profile_birthyear_out_of_range, minValidYear, maxValidYear),
        requiredError = stringResource(R.string.profile_birthyear_required),
        forceShowError = forceShowError
    )
    Spacer(modifier = Modifier.height(20.dp))
    PriceRangeCard(
        titleText = stringResource(R.string.profile_price_range_format, uiState.priceRange.start.toInt(), uiState.priceRange.endInclusive.toInt()),
        priceRange = uiState.priceRange,
        onPriceRangeChanged = onPriceRangeChanged
    )
    Spacer(modifier = Modifier.height(20.dp))
    OnlyReleasedCard(
        title = stringResource(R.string.profile_released_only_title),
        description = if (uiState.onlyReleased) {
            stringResource(R.string.profile_released_only_desc_on)
        } else {
            stringResource(R.string.profile_released_only_desc_off)
        },
        onlyReleased = uiState.onlyReleased,
        onOnlyReleasedChanged = onOnlyReleasedChanged
    )
    Spacer(modifier = Modifier.height(24.dp))
    GenreSelectorSection(
        title = stringResource(R.string.profile_genres),
        availableGenres = GenreUtils.ALL_GENRES,
        selectedGenres = uiState.selectedGenres,
        onGenreToggled = onGenreToggled
    )
    Spacer(modifier = Modifier.height(20.dp))
    Button(
        onClick = onSave,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(stringResource(R.string.profile_save_changes), style = MaterialTheme.typography.titleMedium)
    }
    Spacer(modifier = Modifier.height(24.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.profile_account_options),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        OutlinedButton(onClick = onOpenPasswordDialog, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.profile_change_password))
        }
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedButton(
            onClick = onOpenDeleteDialog,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.profile_delete_account))
        }
    }
}
