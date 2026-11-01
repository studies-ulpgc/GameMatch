package com.pamn.gamematch.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val likedGames: List<String> = emptyList()
)