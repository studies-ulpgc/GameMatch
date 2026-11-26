package com.pamn.gamematch.ui.discover

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.utils.GenreUtils
import com.pamn.gamematch.utils.SortOption

val PLATFORM_OPTIONS = listOf("Todas", "Nintendo", "PlayStation", "Xbox", "PC")

@StringRes
internal fun sortOptionLabelRes(option: SortOption): Int = when (option) {
    SortOption.RELEVANCE -> R.string.discover_sort_relevance
    SortOption.PRICE_ASC -> R.string.discover_sort_price_asc
    SortOption.PRICE_DESC -> R.string.discover_sort_price_desc
    SortOption.RATING -> R.string.discover_sort_rating
    SortOption.VOTES -> R.string.discover_sort_votes
    SortOption.NEWEST -> R.string.discover_sort_newest
}

@Composable
internal fun DiscoverGenreChips(
    availableGenres: List<String>,
    selectedGenres: Set<String>,
    onGenreSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(availableGenres) { genre ->
            val labelText = if (genre == "Todas") {
                stringResource(R.string.discover_platform_all)
            } else {
                GenreUtils.getLocalizedGenre(genre)
            }
            FilterChip(
                selected = selectedGenres.contains(genre),
                onClick = { onGenreSelected(genre) },
                label = { Text(labelText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
internal fun DiscoverPriceFilter(
    maxPrice: Double,
    onMaxPriceChanged: (Double) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.discover_price_max),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (maxPrice >= 100.0) stringResource(R.string.discover_price_any) else "${maxPrice.toInt()} €",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = maxPrice.toFloat(),
            onValueChange = { onMaxPriceChanged(it.toDouble()) },
            valueRange = 0f..100f,
            steps = 19,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun DiscoverSortSelector(
    selectedOption: SortOption,
    onSortSelected: (SortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.discover_sort_title),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Box {
            FilterChip(
                selected = selectedOption != SortOption.RELEVANCE,
                onClick = { expanded = true },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(sortOptionLabelRes(selectedOption)))
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(stringResource(sortOptionLabelRes(option))) },
                        onClick = {
                            onSortSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun DiscoverGameCarousel(
    title: String,
    games: List<Game>,
    likedGameIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onShowInfo: (Game) -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(games, key = { it.id }) { game ->
            DiscoverGameCard(
                game = game,
                isLiked = likedGameIds.contains(game.id),
                onToggleFavorite = { onToggleFavorite(game.id) },
                onShowInfo = { onShowInfo(game) },
                modifier = Modifier.width(200.dp).height(270.dp)
            )
        }
    }
}
