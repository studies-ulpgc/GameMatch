package com.pamn.gamematch

import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.utils.SortOption
import com.pamn.gamematch.utils.sortGames
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSorterTest {

    private val g1 = Game(id = "1", title = "Game 1", price = 10.0, rating = 4.0, ratingCount = 100, releaseDate = "2024-01-01")
    private val g2 = Game(id = "2", title = "Game 2", price = 50.0, rating = 4.5, ratingCount = 50, releaseDate = "2026-05-10")
    private val g3 = Game(id = "3", title = "Game 3", price = null, rating = 4.8, ratingCount = null, releaseDate = "")
    private val g4 = Game(id = "4", title = "Game 4", price = 30.0, rating = 4.5, ratingCount = 200, releaseDate = "2025-08-20")

    @Test
    fun testEmptyListReturnsEmpty() {
        val result = sortGames(emptyList(), SortOption.RELEVANCE)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testRelevanceDoesNotChangeOrder() {
        val list = listOf(g1, g2, g3, g4)
        val result = sortGames(list, SortOption.RELEVANCE)
        assertEquals(list, result)
    }

    @Test
    fun testPriceAscPutsNullLast() {
        val list = listOf(g2, g3, g1, g4)
        val result = sortGames(list, SortOption.PRICE_ASC)
        // Prices: 10.0 (g1), 30.0 (g4), 50.0 (g2), null (g3)
        assertEquals(listOf(g1, g4, g2, g3), result)
    }

    @Test
    fun testPriceDescPutsNullLast() {
        val list = listOf(g1, g3, g2, g4)
        val result = sortGames(list, SortOption.PRICE_DESC)
        // Prices: 50.0 (g2), 30.0 (g4), 10.0 (g1), null (g3)
        assertEquals(listOf(g2, g4, g1, g3), result)
    }

    @Test
    fun testRatingBreaksTiesWithVotes() {
        // g2 and g4 both have rating = 4.5, but g4 has ratingCount = 200 > g2 ratingCount = 50
        val list = listOf(g2, g4)
        val result = sortGames(list, SortOption.RATING)
        assertEquals(listOf(g4, g2), result)
    }

    @Test
    fun testVotesHandlesNullRatingCount() {
        // g3 has ratingCount = null (treated as 0), g1 has ratingCount = 100
        val list = listOf(g3, g1)
        val result = sortGames(list, SortOption.VOTES)
        assertEquals(listOf(g1, g3), result)
    }

    @Test
    fun testNewestSortsIsoDatesAndPutsBlankLast() {
        val list = listOf(g1, g3, g2, g4)
        val result = sortGames(list, SortOption.NEWEST)
        // Dates: "2026-05-10" (g2), "2025-08-20" (g4), "2024-01-01" (g1), "" (g3)
        assertEquals(listOf(g2, g4, g1, g3), result)
    }
}
