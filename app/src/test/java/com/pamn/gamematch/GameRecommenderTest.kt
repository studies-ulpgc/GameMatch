package com.pamn.gamematch

import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.utils.recommendGames
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameRecommenderTest {

    private val g1 = Game(id = "1", title = "Game 1", genre = "Shooter", price = 20.0, isReleased = true, rating = 4.0, ratingCount = 100)
    private val g2 = Game(id = "2", title = "Game 2", genre = "Role-playing (RPG), Adventure", price = 50.0, isReleased = true, rating = 4.5, ratingCount = 50)
    private val g3 = Game(id = "3", title = "Game 3", genre = "Adventure", price = 80.0, isReleased = true, rating = 3.5, ratingCount = 10)
    private val g4 = Game(id = "4", title = "Game 4", genre = "Shooter", price = null, isReleased = true, rating = 4.8, ratingCount = 200)
    private val g5 = Game(id = "5", title = "Game 5", genre = "Puzzle", price = 10.0, isReleased = false, rating = 4.0, ratingCount = 20)

    @Test
    fun testExcludesLikedIds() {
        val result = recommendGames(
            catalog = listOf(g1, g2),
            preferredGenres = listOf("Acción", "Aventura"),
            minPrice = 0.0,
            maxPrice = 100.0,
            onlyReleased = false,
            likedIds = setOf("1"),
            minDeckSize = 0
        )
        assertFalse(result.any { it.id == "1" })
        assertTrue(result.any { it.id == "2" })
    }

    @Test
    fun testOnlyReleasedFilter() {
        val resultOnlyReleased = recommendGames(
            catalog = listOf(g1, g5),
            preferredGenres = emptyList(),
            minPrice = 0.0,
            maxPrice = 100.0,
            onlyReleased = true,
            likedIds = emptySet(),
            minDeckSize = 0
        )
        assertFalse(resultOnlyReleased.any { it.id == "5" })

        val resultIncludeUnreleased = recommendGames(
            catalog = listOf(g1, g5),
            preferredGenres = emptyList(),
            minPrice = 0.0,
            maxPrice = 100.0,
            onlyReleased = false,
            likedIds = emptySet(),
            minDeckSize = 0
        )
        assertTrue(resultIncludeUnreleased.any { it.id == "5" })
    }

    @Test
    fun testPriceAboveMaxPriceExcluded() {
        val result = recommendGames(
            catalog = listOf(g1, g3),
            preferredGenres = emptyList(),
            minPrice = 0.0,
            maxPrice = 50.0,
            onlyReleased = true,
            likedIds = emptySet(),
            minDeckSize = 0
        )
        assertTrue(result.any { it.id == "1" })
        assertFalse(result.any { it.id == "3" })
    }

    @Test
    fun testPriceNullPassesButSortedAfterKnownPrice() {
        val result = recommendGames(
            catalog = listOf(g4, g1),
            preferredGenres = listOf("Acción"),
            minPrice = 0.0,
            maxPrice = 50.0,
            onlyReleased = true,
            likedIds = emptySet(),
            minDeckSize = 0
        )
        assertEquals(2, result.size)
        // g1 has known price 20.0, g4 has price = null. g1 should be first despite g4 higher rating
        assertEquals("1", result[0].id)
        assertEquals("4", result[1].id)
    }

    @Test
    fun testMaxPrice100HasNoUpperLimit() {
        val result = recommendGames(
            catalog = listOf(g1, g3),
            preferredGenres = emptyList(),
            minPrice = 0.0,
            maxPrice = 100.0,
            onlyReleased = true,
            likedIds = emptySet(),
            minDeckSize = 0
        )
        assertEquals(2, result.size)
    }

    @Test
    fun testEmptyPreferredGenresDoesNotFilterByGenre() {
        val result = recommendGames(
            catalog = listOf(g1, g2, g5),
            preferredGenres = emptyList(),
            minPrice = 0.0,
            maxPrice = 100.0,
            onlyReleased = false,
            likedIds = emptySet(),
            minDeckSize = 0
        )
        assertEquals(3, result.size)
    }

    @Test
    fun testTwoMatchingGenresSortedBeforeOneMatchingGenre() {
        // g2 shares 2 genres ("RPG", "Aventura"), g3 shares 1 genre ("Aventura")
        val result = recommendGames(
            catalog = listOf(g3, g2),
            preferredGenres = listOf("RPG", "Aventura"),
            minPrice = 0.0,
            maxPrice = 100.0,
            onlyReleased = true,
            likedIds = emptySet(),
            minDeckSize = 0
        )
        assertEquals("2", result[0].id)
        assertEquals("3", result[1].id)
    }

    @Test
    fun testMinDeckSizeBackfill() {
        // g2 matches "RPG", g1 & g3 do not match "RPG". With minDeckSize = 3, returns 3 items with g2 first.
        val result = recommendGames(
            catalog = listOf(g1, g2, g3),
            preferredGenres = listOf("RPG"),
            minPrice = 0.0,
            maxPrice = 100.0,
            onlyReleased = true,
            likedIds = emptySet(),
            minDeckSize = 3
        )
        assertEquals(3, result.size)
        assertEquals("2", result[0].id)
    }
}
