package com.pamn.gamematch.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.ui.swipe.GameDetailModal
import com.pamn.gamematch.ui.theme.SteamAccentBlue
import com.pamn.gamematch.ui.theme.UnreleasedBadgeColor

val PLATFORM_OPTIONS = listOf("Todas", "Nintendo", "PlayStation", "Xbox", "PC")

@Composable
fun DiscoverScreen(
    modifier: Modifier = Modifier,
    onNavigateToFullDetails: (String) -> Unit = {},
    viewModel: DiscoverViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedGameForModal by remember { mutableStateOf<Game?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    // Un único LazyColumn vertical para desplazar toda la pantalla arriba y abajo suavemente
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Título de la pantalla
        item {
            Text(
                text = "Descubrir",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // 1. Barra de búsqueda superior
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar juego o género...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar búsqueda"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        // 2. Filtros de Plataformas (Chips)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PLATFORM_OPTIONS) { platform ->
                    val isSelected = uiState.selectedPlatform.equals(platform, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onPlatformSelected(platform) },
                        label = { Text(platform) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        // 3. Selector Rango de Precio (Slider)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Precio máx:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (uiState.maxPrice >= 100.0) "Cualquiera" else "${uiState.maxPrice.toInt()} €",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = uiState.maxPrice.toFloat(),
                    onValueChange = { viewModel.onMaxPriceChanged(it.toDouble()) },
                    valueRange = 0f..100f,
                    steps = 19,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 4. Estado de Carga / Resultados / Carruseles
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (uiState.isFilteringActive) {
            // Muestra los resultados filtrados
            item {
                Text(
                    text = "Resultados (${uiState.filteredGames.size})",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (uiState.filteredGames.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron juegos con estos filtros.",
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
                        for (game in rowGames) {
                            DiscoverGameCard(
                                game = game,
                                isLiked = uiState.likedGameIds.contains(game.id),
                                onToggleFavorite = { viewModel.toggleFavorite(game.id) },
                                onShowInfo = { selectedGameForModal = game },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.7f)
                            )
                        }
                        if (rowGames.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            // Carruseles horizontales (+ Destacado y + Trending)
            item {
                Text(
                    text = "+ Destacado 🔥",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.featuredGames, key = { it.id }) { game ->
                        DiscoverGameCard(
                            game = game,
                            isLiked = uiState.likedGameIds.contains(game.id),
                            onToggleFavorite = { viewModel.toggleFavorite(game.id) },
                            onShowInfo = { selectedGameForModal = game },
                            modifier = Modifier
                                .width(200.dp)
                                .height(270.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "+ Trending 📈",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.trendingGames, key = { it.id }) { game ->
                        DiscoverGameCard(
                            game = game,
                            isLiked = uiState.likedGameIds.contains(game.id),
                            onToggleFavorite = { viewModel.toggleFavorite(game.id) },
                            onShowInfo = { selectedGameForModal = game },
                            modifier = Modifier
                                .width(200.dp)
                                .height(270.dp)
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

/** Tarjeta de juego para Discover que usa el mismo icono y estilos de favorito que FavoritesScreen. */
@Composable
fun DiscoverGameCard(
    game: Game,
    isLiked: Boolean,
    onToggleFavorite: () -> Unit,
    onShowInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = if (game.imageUrl.isBlank()) "https://cdn.cloudflare.steamstatic.com/steam/apps/1091500/header.jpg" else game.imageUrl,
                contentDescription = game.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Velo degradado inferior para legibilidad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                bgColor.copy(alpha = 0.75f),
                                bgColor
                            )
                        )
                    )
            )

            // Título, género y precio
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor
                )
                Text(
                    text = game.genre,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = SteamAccentBlue,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (game.price == 0.0) "Gratis" else "${game.price} €",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black
                        )
                    }

                    if (!game.isReleased) {
                        Surface(
                            color = UnreleasedBadgeColor,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Próximamente",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Botones superiores: Info (ℹ️) y Favorito (❤️) - Idéntico a FavoritesScreen
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Botón Info (ℹ️)
                IconButton(
                    onClick = onShowInfo,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Información",
                        tint = SteamAccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Botón Corazón (Favorito) - Idéntico al de FavoritesScreen
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = if (isLiked) "Quitar de favoritos" else "Añadir a favoritos",
                        tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}