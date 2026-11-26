package com.pamn.gamematch.ui.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pamn.gamematch.R
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.ui.AppViewModelProvider
import com.pamn.gamematch.ui.components.SearchBarField
import com.pamn.gamematch.ui.detail.GameDetailModal

@Composable
fun DiscoverScreen(
    modifier: Modifier = Modifier,
    onNavigateToFullDetails: (String) -> Unit = {},
    viewModel: DiscoverViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedGameForModal by remember { mutableStateOf<Game?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.discover_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        item {
            SearchBarField(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                placeholder = stringResource(R.string.discover_search_hint),
                searchContentDescription = stringResource(R.string.discover_search_cd),
                clearContentDescription = stringResource(R.string.discover_search_clear),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            DiscoverGenreChips(
                availableGenres = uiState.availableGenres,
                selectedGenres = uiState.selectedGenres,
                onGenreSelected = viewModel::onGenreSelected
            )
        }
        item {
            DiscoverPriceFilter(
                maxPrice = uiState.maxPrice,
                onMaxPriceChanged = viewModel::onMaxPriceChanged
            )
        }
        item {
            DiscoverSortSelector(
                selectedOption = uiState.sortOption,
                onSortSelected = viewModel::onSortSelected
            )
        }

        when {
            uiState.isLoading -> item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.isFilteringActive -> {
                item {
                    Text(
                        text = stringResource(R.string.discover_results, uiState.filteredGames.size),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                if (uiState.filteredGames.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.discover_empty),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(uiState.filteredGames.chunked(2)) { rowGames ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowGames.forEach { game ->
                                DiscoverGameCard(
                                    game = game,
                                    isLiked = uiState.likedGameIds.contains(game.id),
                                    onToggleFavorite = { viewModel.toggleFavorite(game.id) },
                                    onShowInfo = { selectedGameForModal = game },
                                    modifier = Modifier.weight(1f).aspectRatio(0.7f)
                                )
                            }
                            if (rowGames.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            else -> {
                if (uiState.featuredGames.isNotEmpty()) {
                    item {
                        DiscoverGameCarousel(
                            title = stringResource(R.string.discover_section_featured),
                            games = uiState.featuredGames,
                            likedGameIds = uiState.likedGameIds,
                            onToggleFavorite = viewModel::toggleFavorite,
                            onShowInfo = { selectedGameForModal = it }
                        )
                    }
                }
                if (uiState.trendingGames.isNotEmpty()) {
                    item {
                        DiscoverGameCarousel(
                            title = stringResource(R.string.discover_section_trending),
                            games = uiState.trendingGames,
                            likedGameIds = uiState.likedGameIds,
                            onToggleFavorite = viewModel::toggleFavorite,
                            onShowInfo = { selectedGameForModal = it }
                        )
                    }
                }
            }
        }
    }

    selectedGameForModal?.let { game ->
        GameDetailModal(
            game = game,
            onDismiss = { selectedGameForModal = null },
            onNavigateToFullDetails = { gameId ->
                selectedGameForModal = null
                onNavigateToFullDetails(gameId)
            }
        )
    }
}
