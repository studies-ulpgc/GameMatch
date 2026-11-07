package com.pamn.gamematch.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pamn.gamematch.R

object GenreUtils {
    val ALL_GENRES = listOf("Acción", "Aventura", "RPG", "Indie", "Estrategia", "Deportes", "Terror")

    @Composable
    fun getLocalizedGenre(genre: String): String {
        return when (genre.trim().lowercase()) {
            "acción", "accion", "action" -> stringResource(R.string.genre_action)
            "aventura", "adventure" -> stringResource(R.string.genre_adventure)
            "rpg" -> stringResource(R.string.genre_rpg)
            "indie" -> stringResource(R.string.genre_indie)
            "estrategia", "strategy" -> stringResource(R.string.genre_strategy)
            "deportes", "sports" -> stringResource(R.string.genre_sports)
            "terror", "horror" -> stringResource(R.string.genre_horror)
            else -> genre
        }
    }
}
