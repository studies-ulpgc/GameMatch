package com.pamn.gamematch.utils

object GenreMatcher {
    fun canonicalGenres(raw: String): Set<String> {
        if (raw.isBlank()) return emptySet()

        val result = mutableSetOf<String>()
        val tokens = raw.split(",").map { it.trim() }

        for (token in tokens) {
            val lower = token.lowercase()
            when {
                lower.contains("acción") || lower.contains("accion") || lower.contains("action") ||
                lower.contains("hack and slash") || lower.contains("beat 'em up") || lower.contains("shooter") ||
                lower.contains("fighting") -> result.add("Acción")

                lower.contains("aventura") || lower.contains("adventure") || lower.contains("point-and-click") ->
                    result.add("Aventura")

                lower.contains("rpg") || lower.contains("role-playing") ->
                    result.add("RPG")

                lower.contains("indie") ->
                    result.add("Indie")

                lower.contains("estrategia") || lower.contains("strategy") || lower.contains("tactical") ||
                lower.contains("tbs") || lower.contains("real time strategy") || lower.contains("real-time strategy") ->
                    result.add("Estrategia")

                lower.contains("deportes") || lower.contains("sports") || lower.contains("sport") || lower.contains("racing") ->
                    result.add("Deportes")

                lower.contains("terror") || lower.contains("horror") ->
                    result.add("Terror")
            }
        }
        return result
    }
}
