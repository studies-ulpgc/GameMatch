package com.pamn.gamematch.data.model

data class Game(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val genre: String = "",
    val price: Double = 0.0,
    val releaseDate: String = "Ya disponible",
    val isReleased: Boolean = true,
    val platforms: List<String> = listOf("PC", "PlayStation", "Xbox", "Nintendo"),
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false
)