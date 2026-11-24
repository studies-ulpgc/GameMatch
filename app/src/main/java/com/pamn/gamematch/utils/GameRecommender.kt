package com.pamn.gamematch.utils

import com.pamn.gamematch.data.model.Game

fun recommendGames(
    catalog: List<Game>,
    preferredGenres: List<String>,
    minPrice: Double,
    maxPrice: Double,
    onlyReleased: Boolean,
    likedIds: Set<String>,
    minDeckSize: Int = 30
): List<Game> {
    val candidates = catalog.filter { game ->
        !likedIds.contains(game.id) && (!onlyReleased || game.isReleased)
    }

    val prefGenresSet = preferredGenres.toSet()

    val comparator = Comparator<Game> { g1, g2 ->
        val g1GenresCount = if (prefGenresSet.isEmpty()) 0 else GenreMatcher.canonicalGenres(g1.genre).intersect(prefGenresSet).size
        val g2GenresCount = if (prefGenresSet.isEmpty()) 0 else GenreMatcher.canonicalGenres(g2.genre).intersect(prefGenresSet).size

        if (g1GenresCount != g2GenresCount) {
            return@Comparator g2GenresCount.compareTo(g1GenresCount)
        }

        val g1HasPrice = if (g1.price != null) 1 else 0
        val g2HasPrice = if (g2.price != null) 1 else 0
        if (g1HasPrice != g2HasPrice) {
            return@Comparator g2HasPrice.compareTo(g1HasPrice)
        }

        if (g1.rating != g2.rating) {
            return@Comparator g2.rating.compareTo(g1.rating)
        }

        val g1Votes = g1.ratingCount ?: 0
        val g2Votes = g2.ratingCount ?: 0
        g2Votes.compareTo(g1Votes)
    }

    val recommended = candidates.filter { game ->
        val matchesGenre = prefGenresSet.isEmpty() ||
                GenreMatcher.canonicalGenres(game.genre).any { prefGenresSet.contains(it) }

        val matchesPrice = game.price == null || (game.price >= minPrice && (maxPrice >= 100.0 || game.price <= maxPrice))

        matchesGenre && matchesPrice
    }.sortedWith(comparator)

    if (recommended.size >= minDeckSize) {
        return recommended
    }

    val recommendedIds = recommended.map { it.id }.toSet()
    val remaining = candidates
        .filterNot { recommendedIds.contains(it.id) }
        .sortedWith(comparator)

    val needed = minDeckSize - recommended.size
    return recommended + remaining.take(needed)
}
