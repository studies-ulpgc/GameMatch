package com.pamn.gamematch.data.remote.mapper

import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.remote.dto.igdb.IgdbGameDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun IgdbGameDto.toDomain(): Game {
    val mappedGenres = genres?.mapNotNull { it.name.ifBlank { null } }?.joinToString(", ")
        ?.ifBlank { "Varios" } ?: "Varios"

    val mappedPlatforms = platforms?.mapNotNull { it.name.ifBlank { null } }
        ?.ifEmpty { listOf("PC", "PlayStation", "Xbox", "Nintendo") }
        ?: listOf("PC", "PlayStation", "Xbox", "Nintendo")

    val formattedReleaseDate = if (firstReleaseDate != null && firstReleaseDate > 0) {
        val date = Date(firstReleaseDate * 1000L)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(date)
    } else {
        "Por anunciar"
    }

    val coverUrl = cover?.imageId?.let { imgId ->
        "https://images.igdb.com/igdb/image/upload/t_720p/$imgId.jpg"
    } ?: "https://cdn.cloudflare.steamstatic.com/steam/apps/1091500/header.jpg"

    val steamAppId = externalGames
        ?.firstOrNull { it.externalGameSource == 1 || it.category == 1 }
        ?.uid?.toIntOrNull()

    // IGDB rating es de 0 a 100. Escalar a formato 0.0 - 5.0 para GameMatch
    val rawRating = rating ?: 0.0
    val normalizedRating = (rawRating / 20.0).let { Math.round(it * 10.0) / 10.0 }
    val finalRating = if (normalizedRating <= 0.0) 4.2 else normalizedRating

    return Game(
        id = id.toString(),
        title = name,
        description = summary ?: "",
        imageUrl = coverUrl,
        genre = mappedGenres,
        price = null,
        releaseDate = formattedReleaseDate,
        isReleased = firstReleaseDate != null && (firstReleaseDate * 1000L) <= System.currentTimeMillis(),
        platforms = mappedPlatforms,
        steamAppId = steamAppId,
        rating = finalRating,
        isFeatured = finalRating >= 3.8,
        isTrending = finalRating >= 3.5
    )
}
