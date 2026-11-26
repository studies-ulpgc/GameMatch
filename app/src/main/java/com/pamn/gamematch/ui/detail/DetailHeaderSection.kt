package com.pamn.gamematch.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.ui.components.priceLabel
import com.pamn.gamematch.ui.theme.GameMatchTheme
import com.pamn.gamematch.ui.theme.SteamAccentBlue
import com.pamn.gamematch.utils.GenreUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DetailHeaderSection(
    game: Game,
    uiState: GameDetailUiState,
    onOpenSteamStore: (Int) -> Unit
) {
    if (game.imageUrl.isNotBlank()) {
        AsyncImage(
            model = game.imageUrl,
            contentDescription = game.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.8f)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.detail_genre, GenreUtils.getLocalizedGenre(game.genre)),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            Surface(color = SteamAccentBlue, shape = RoundedCornerShape(6.dp)) {
                if (uiState.isPriceLoading) {
                    Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.Black, strokeWidth = 2.dp)
                    }
                } else {
                    Text(
                        text = priceLabel(uiState.price),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (game.steamAppId != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(6.dp),
                    onClick = { onOpenSteamStore(game.steamAppId) }
                ) {
                    Text(
                        text = stringResource(R.string.detail_steam_badge),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = stringResource(R.string.detail_rating_cd),
            tint = GameMatchTheme.semanticColors.starYellow,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = if (uiState.reviews.isNotEmpty()) {
                "%.1f / 5 (%d)".format(uiState.averageRating, uiState.reviews.size)
            } else {
                stringResource(R.string.detail_no_reviews)
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text(text = game.description, style = MaterialTheme.typography.bodyMedium)

    if (game.steamAppId != null) {
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(onClick = { onOpenSteamStore(game.steamAppId) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.detail_steam_button))
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider()
}
