package com.pamn.gamematch.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var gameDao: GameDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        gameDao = database.gameDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun entity(
        id: String,
        title: String,
        platforms: List<String> = listOf("PC", "Nintendo")
    ) = GameEntity(
        id = id,
        title = title,
        description = "Description for $title",
        imageUrl = "https://example.com/$id.jpg",
        genre = "Action, RPG",
        price = 29.99,
        releaseDate = "2024",
        isReleased = true,
        platforms = platforms,
        steamAppId = 12345,
        rating = 9.0,
        ratingCount = 500,
        isFeatured = true,
        isTrending = false,
        cachedAt = 1000L
    )

    @Test
    fun upsertAll_insertsGamesAndPreservesPlatformsConverter() = runBlocking {
        val games = listOf(
            entity("1", "Hades", listOf("PC", "PlayStation")),
            entity("2", "Celeste", listOf("PC", "Xbox", "Nintendo"))
        )

        gameDao.upsertAll(games)

        val stored = gameDao.getAll()
        assertEquals(2, gameDao.count())
        assertEquals(2, stored.size)
        assertEquals(listOf("PC", "PlayStation"), stored.first { it.id == "1" }.platforms)
        assertEquals(listOf("PC", "Xbox", "Nintendo"), stored.first { it.id == "2" }.platforms)
    }

    @Test
    fun upsertAll_withExistingPrimaryKey_replacesRecord() = runBlocking {
        gameDao.upsertAll(listOf(entity("1", "Old Title")))
        gameDao.upsertAll(listOf(entity("1", "Updated Title")))

        val stored = gameDao.getAll()
        assertEquals(1, gameDao.count())
        assertEquals("Updated Title", stored.single().title)
    }

    @Test
    fun clear_removesAllGames() = runBlocking {
        gameDao.upsertAll(listOf(entity("1", "Hades"), entity("2", "Hollow Knight")))
        assertEquals(2, gameDao.count())

        gameDao.clear()

        assertEquals(0, gameDao.count())
        assertTrue(gameDao.getAll().isEmpty())
    }
}
