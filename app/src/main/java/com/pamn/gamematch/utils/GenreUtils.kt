package com.pamn.gamematch.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pamn.gamematch.R

object GenreUtils {
    val ALL_GENRES = listOf("Acción", "Aventura", "RPG", "Indie", "Estrategia", "Deportes", "Terror")

    @Composable
    fun getLocalizedGenre(genre: String): String {
        if (genre.isBlank()) return stringResource(R.string.genre_action)

        val tokens = genre.split(",").map { it.trim() }
        return tokens.map { token ->
            localizeSingleGenre(token)
        }.distinct().joinToString(", ")
    }

    @Composable
    private fun localizeSingleGenre(singleGenre: String): String {
        val lower = singleGenre.lowercase()
        return when {
            lower.contains("acción") || lower.contains("accion") || lower.contains("action") ||
            lower.contains("hack and slash") || lower.contains("beat 'em up") || lower.contains("shooter") ||
            lower.contains("fighting") -> stringResource(R.string.genre_action)

            lower.contains("aventura") || lower.contains("adventure") || lower.contains("point-and-click") ->
                stringResource(R.string.genre_adventure)

            lower.contains("rpg") || lower.contains("role-playing") ->
                stringResource(R.string.genre_rpg)

            lower.contains("indie") ->
                stringResource(R.string.genre_indie)

            lower.contains("estrategia") || lower.contains("strategy") || lower.contains("tactical") ||
            lower.contains("tbs") || lower.contains("rts") ->
                stringResource(R.string.genre_strategy)

            lower.contains("deportes") || lower.contains("sports") || lower.contains("sport") || lower.contains("racing") ->
                stringResource(R.string.genre_sports)

            lower.contains("terror") || lower.contains("horror") ->
                stringResource(R.string.genre_horror)

            else -> singleGenre
        }
    }
}
