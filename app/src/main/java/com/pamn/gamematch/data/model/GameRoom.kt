package com.pamn.gamematch.data.model

data class GameRoom(
    val id: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val gameTitle: String = "",
    val description: String = "",
    val platform: String = "PC",
    val contactInfo: String = "",
    val maxPlayers: Int = 4,
    val currentPlayers: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val isFollowersOnly: Boolean = false,
    val members: List<String> = emptyList()
)