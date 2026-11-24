package com.pamn.gamematch.ui.swipe

import com.pamn.gamematch.data.model.Game

object SwipePreviewData {
    val samplePreviewGames: List<Game> = listOf(
        Game(
            id = "1",
            title = "The Legend of Zelda: Tears of the Kingdom",
            description = "Aventura épica de exploración e investigación en un vasto mundo abierto sobre las tierras y los cielos de Hyrule.",
            imageUrl = "https://cdn.cloudflare.steamstatic.com/steam/apps/1245620/header.jpg",
            genre = "Aventura",
            price = 59.99,
            releaseDate = "Disponible",
            isReleased = true,
            platforms = listOf("Nintendo"),
            isFeatured = true,
            isTrending = false
        ),
        Game(
            id = "2",
            title = "GTA VI",
            description = "Siguiente entrega de la aclamada franquicia de acción y crimen en un impresionante mundo abierto hiperrealista.",
            imageUrl = "https://cdn.cloudflare.steamstatic.com/steam/apps/1091500/header.jpg",
            genre = "Acción",
            price = 79.99,
            releaseDate = "Lanzamiento: Otoño 2026",
            isReleased = false,
            platforms = listOf("PlayStation", "Xbox"),
            isFeatured = true,
            isTrending = true
        )
    )
}
