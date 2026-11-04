package com.pamn.gamematch.data.model

data class GameReview(
    val id: String = "",
    val gameId: String = "",
    val gameTitle: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)