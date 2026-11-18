package com.pamn.gamematch

import com.pamn.gamematch.data.local.mapper.toDomain
import com.pamn.gamematch.data.local.mapper.toEntity
import com.pamn.gamematch.data.model.Game
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameEntityMapperTest {

    @Test
    fun testGameEntityMapper_roundTrip() {
        val originalGame = Game(
            id = "test-id-123",
            title = "Test Game",
            description = "Test Description",
            imageUrl = "https://example.com/cover.jpg",
            genre = "Acción",
            price = null,
            releaseDate = "2026-01-01",
            isReleased = true,
            platforms = listOf("PC", "PlayStation"),
            steamAppId = null,
            rating = 4.5,
            ratingCount = null,
            isFeatured = true,
            isTrending = false
        )

        val entity = originalGame.toEntity(cachedAt = 1000L)
        assertEquals("test-id-123", entity.id)
        assertNull(entity.price)
        assertNull(entity.steamAppId)
        assertNull(entity.ratingCount)
        assertEquals(listOf("PC", "PlayStation"), entity.platforms)
        assertEquals(1000L, entity.cachedAt)

        val mappedBackGame = entity.toDomain()
        assertEquals(originalGame, mappedBackGame)
    }
}
