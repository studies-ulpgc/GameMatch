package com.pamn.gamematch.data.model

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val birthYear: String = "",
    val language: String = "Español",
    val minPrice: Double = 0.0,
    val maxPrice: Double = 100.0,
    val onlyReleased: Boolean = true,
    val isDarkMode: Boolean = true,
    val preferredGenres: List<String> = emptyList(),
    val profilePictureUrl: String = "",
    // Campos calculados locales para afinidad en la pantalla de Comunidad
    val matchPercentage: Int = 0,
    val commonGenres: List<String> = emptyList()
)