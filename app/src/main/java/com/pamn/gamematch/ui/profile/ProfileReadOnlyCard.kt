package com.pamn.gamematch.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.R
import com.pamn.gamematch.utils.GenreUtils

@Composable
fun ProfileReadOnlyCard(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_personal_info),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            ProfileInfoRow(
                label = stringResource(R.string.profile_username),
                value = uiState.username.ifBlank { stringResource(R.string.profile_unspecified) }
            )
            ProfileInfoRow(
                label = stringResource(R.string.profile_email),
                value = uiState.email.ifBlank { stringResource(R.string.profile_not_available) }
            )
            ProfileInfoRow(
                label = stringResource(R.string.profile_password),
                value = "••••••••••••"
            )
            ProfileInfoRow(
                label = stringResource(R.string.profile_birthyear),
                value = uiState.birthYear.ifBlank { stringResource(R.string.profile_unspecified) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
            )

            Text(
                text = stringResource(R.string.profile_game_prefs),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            ProfileInfoRow(
                label = stringResource(R.string.profile_price_range),
                value = "${uiState.priceRange.start.toInt()} € - ${uiState.priceRange.endInclusive.toInt()} €"
            )
            ProfileInfoRow(
                label = stringResource(R.string.profile_released_only),
                value = if (uiState.onlyReleased) stringResource(R.string.profile_yes) else stringResource(R.string.profile_no_upcoming)
            )
            ProfileInfoRow(
                label = stringResource(R.string.profile_genres),
                value = if (uiState.selectedGenres.isEmpty()) {
                    stringResource(R.string.profile_all)
                } else {
                    uiState.selectedGenres.map { GenreUtils.getLocalizedGenre(it) }.joinToString(", ")
                }
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
