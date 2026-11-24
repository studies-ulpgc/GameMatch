package com.pamn.gamematch.utils

fun platformFamily(name: String): String? {
    val trimmed = name.trim()
    val lower = trimmed.lowercase()

    return when {
        trimmed == "PC (Microsoft Windows)" || trimmed == "Mac" || trimmed == "Linux" || trimmed == "DOS" -> "PC"
        lower.startsWith("playstation") -> "PlayStation"
        lower.startsWith("xbox") -> "Xbox"
        lower.contains("nintendo") || lower.startsWith("game boy") ||
                trimmed in setOf("Wii", "Wii U", "Super Famicom", "Family Computer", "Satellaview") -> "Nintendo"
        else -> null
    }
}
