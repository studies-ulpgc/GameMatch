package com.pamn.gamematch.data.remote.mapper

import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.remote.dto.rawg.RawgGameDto

fun RawgGameDto.toDomain(): Game {
    val mappedGenres = genres?.mapNotNull { it.name.ifBlank { null } }?.joinToString(", ")
        ?.ifBlank { "Varios" } ?: "Varios"

    val mappedPlatforms = platforms?.mapNotNull { wrapper ->
        wrapper.platform?.name?.ifBlank { null }
    } ?: listOf("PC", "PlayStation", "Xbox", "Nintendo")

    val formattedReleaseDate = released?.ifBlank { "Por anunciar" } ?: "Por anunciar"

    return Game(
        id = id.toString(),
        title = name,
        description = "",
        imageUrl = backgroundImage ?: "https://cdn.cloudflare.steamstatic.com/steam/apps/1091500/header.jpg",
        genre = mappedGenres,
        price = null,
        releaseDate = formattedReleaseDate,
        isReleased = !released.isNullOrBlank(),
        platforms = mappedPlatforms,
        steamAppId = null,
        rating = rating ?: 0.0,
        isFeatured = (rating ?: 0.0) >= 4.0,
        isTrending = (rating ?: 0.0) >= 3.5
    )
}