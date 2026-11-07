package com.pamn.gamematch.data.model

import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val likedGames: List<String> = emptyList(),
    val preferredGenres: List<String> = emptyList(),
    val minPrice: Float = 0f,
    val maxPrice: Float = 70f,
    val onlyReleased: Boolean = true,
    val birthYear: String = "",
    @get:PropertyName("isDarkMode") @field:PropertyName("isDarkMode")
    val isDarkMode: Boolean = true,
    val language: String = "es",
    val profilePictureUrl: String = "https://api.dicebear.com/7.x/bottts/png?seed=default"
)
