package com.pamn.gamematch.utils

import com.pamn.gamematch.data.model.Game

enum class SortOption {
    RELEVANCE,
    PRICE_ASC,
    PRICE_DESC,
    RATING,
    VOTES,
    NEWEST
}

fun sortGames(games: List<Game>, option: SortOption): List<Game> {
    if (games.isEmpty()) return emptyList()

    return when (option) {
        SortOption.RELEVANCE -> games
        SortOption.PRICE_ASC -> games.sortedWith(
            Comparator { g1, g2 ->
                if (g1.price == null && g2.price == null) {
                    g2.rating.compareTo(g1.rating)
                } else if (g1.price == null) {
                    1
                } else if (g2.price == null) {
                    -1
                } else {
                    val priceComp = g1.price.compareTo(g2.price)
                    if (priceComp != 0) priceComp else g2.rating.compareTo(g1.rating)
                }
            }
        )
        SortOption.PRICE_DESC -> games.sortedWith(
            Comparator { g1, g2 ->
                if (g1.price == null && g2.price == null) {
                    g2.rating.compareTo(g1.rating)
                } else if (g1.price == null) {
                    1
                } else if (g2.price == null) {
                    -1
                } else {
                    val priceComp = g2.price.compareTo(g1.price)
                    if (priceComp != 0) priceComp else g2.rating.compareTo(g1.rating)
                }
            }
        )
        SortOption.RATING -> games.sortedWith(
            Comparator { g1, g2 ->
                val ratingComp = g2.rating.compareTo(g1.rating)
                if (ratingComp != 0) {
                    ratingComp
                } else {
                    val v1 = g1.ratingCount ?: 0
                    val v2 = g2.ratingCount ?: 0
                    v2.compareTo(v1)
                }
            }
        )
        SortOption.VOTES -> games.sortedWith(
            Comparator { g1, g2 ->
                val v1 = g1.ratingCount ?: 0
                val v2 = g2.ratingCount ?: 0
                val votesComp = v2.compareTo(v1)
                if (votesComp != 0) {
                    votesComp
                } else {
                    g2.rating.compareTo(g1.rating)
                }
            }
        )
        SortOption.NEWEST -> games.sortedWith(
            Comparator { g1, g2 ->
                val d1 = g1.releaseDate.trim()
                val d2 = g2.releaseDate.trim()
                if (d1.isBlank() && d2.isBlank()) {
                    g2.rating.compareTo(g1.rating)
                } else if (d1.isBlank()) {
                    1
                } else if (d2.isBlank()) {
                    -1
                } else {
                    val dateComp = d2.compareTo(d1)
                    if (dateComp != 0) dateComp else g2.rating.compareTo(g1.rating)
                }
            }
        )
    }
}
