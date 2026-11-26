package com.pamn.gamematch.data.repository

import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.gson.JsonParser
import com.pamn.gamematch.data.local.GameDao
import com.pamn.gamematch.data.local.GameEntity
import com.pamn.gamematch.data.model.GameReview
import com.pamn.gamematch.data.model.User
import com.pamn.gamematch.data.remote.RetrofitClient
import com.pamn.gamematch.data.remote.api.SteamApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameRepositoryTest {

    private val db: FirebaseFirestore = mockk(relaxed = true)
    private val auth: FirebaseAuth = mockk(relaxed = true)
    private val gameDao: GameDao = mockk(relaxed = true)
    private val user: FirebaseUser = mockk(relaxed = true)
    private val usersCol: CollectionReference = mockk(relaxed = true)
    private val userDoc: DocumentReference = mockk(relaxed = true)
    private val gamesCol: CollectionReference = mockk(relaxed = true)
    private val reviewsCol: CollectionReference = mockk(relaxed = true)
    private val steamApi: SteamApiService = mockk(relaxed = true)
    private lateinit var repository: GameRepository

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        mockkObject(RetrofitClient)
        every { RetrofitClient.steamApiService } returns steamApi

        every { auth.currentUser } returns user
        every { user.uid } returns "uid-1"
        every { db.collection("users") } returns usersCol
        every { usersCol.document("uid-1") } returns userDoc
        every { db.collection(GameRepository.GAMES_COLLECTION) } returns gamesCol
        every { db.collection("reviews") } returns reviewsCol

        repository = GameRepository(db, auth, gameDao)
    }

    @After
    fun tearDown() {
        unmockkObject(RetrofitClient)
        unmockkStatic(Log::class)
    }

    private fun <T> successTask(result: T): Task<T> {
        val task = mockk<Task<T>>(relaxed = true)
        every { task.isComplete } returns true
        every { task.isSuccessful } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns result
        val slot = slot<OnSuccessListener<in T>>()
        every { task.addOnSuccessListener(capture(slot)) } answers { slot.captured.onSuccess(result); task }
        return task
    }

    private fun <T> failureTask(ex: Exception): Task<T> {
        val task = mockk<Task<T>>(relaxed = true)
        every { task.isComplete } returns true
        every { task.isSuccessful } returns false
        every { task.isCanceled } returns false
        every { task.exception } returns ex
        every { task.addOnSuccessListener(any()) } returns task
        val slot = slot<OnFailureListener>()
        every { task.addOnFailureListener(capture(slot)) } answers { slot.captured.onFailure(ex); task }
        return task
    }

    @Test
    fun getSteamPrice_parsesPaidFreeAndHandlesFailure() = runTest {
        val paidJson = JsonParser.parseString("""{"success":true,"data":{"price_overview":{"final":1999}}}""")
        coEvery { steamApi.getAppDetailsBatch(appIds = "10") } returns mapOf("10" to paidJson)
        assertEquals(19.99, repository.getSteamPrice(10).getOrNull()!!, 0.001)

        val emptyBatch = JsonParser.parseString("""{"success":true,"data":[]}""")
        val freeJson = JsonParser.parseString("""{"success":true,"data":{"is_free":true}}""")
        coEvery { steamApi.getAppDetailsBatch(appIds = "20") } returns mapOf("20" to emptyBatch)
        coEvery { steamApi.getAppDetailsBasic(appIds = "20") } returns mapOf("20" to freeJson)
        assertEquals(0.0, repository.getSteamPrice(20).getOrNull()!!, 0.001)

        coEvery { steamApi.getAppDetailsBatch(appIds = "30") } throws RuntimeException("timeout")
        assertTrue(repository.getSteamPrice(30).isFailure)
    }

    @Test
    fun getGamesCatalog_usesValidRoomCacheAndAppliesFilters() = runTest {
        val cached = listOf(
            GameEntity("1", "Hades", "Desc", "img", "Action, Indie", 20.0, "2020", true, listOf("PC"), 1, 9.0, 100, true, false, System.currentTimeMillis()),
            GameEntity("2", "Celeste", "Desc", "img", "Indie", 15.0, "2018", true, listOf("PC"), 2, 9.2, 200, false, true, System.currentTimeMillis())
        )
        coEvery { gameDao.getAll() } returns cached

        val filtered = repository.getGamesCatalog(searchQuery = "hades", genres = "Action").getOrThrow()
        assertEquals(1, filtered.size)
        assertEquals("Hades", filtered.first().title)
        verify(exactly = 0) { db.collection(GameRepository.GAMES_COLLECTION) }
    }

    @Test
    fun getGamesCatalog_refreshesExpiredCacheFromFirestoreAndFallsBackOnError() = runTest {
        val stale = GameEntity("1", "Old", "Desc", "img", "RPG", 10.0, "2020", true, listOf("PC"), null, 8.0, 10, false, false, 1L)
        coEvery { gameDao.getAll() } returns listOf(stale)

        val doc = mockk<DocumentSnapshot>(relaxed = true)
        every { doc.id } returns "10"
        every { doc.getString("title") } returns "Elden Ring"
        every { doc.getString("genre") } returns "RPG"
        every { doc.getDouble("price") } returns 59.99
        val snapshot = mockk<QuerySnapshot>(relaxed = true)
        every { snapshot.documents } returns listOf(doc)
        val query = mockk<Query>(relaxed = true)
        every { gamesCol.limit(any()) } returns query
        every { query.get() } returns successTask(snapshot)

        val fresh = repository.getGamesCatalog().getOrThrow()
        assertEquals(listOf("Elden Ring"), fresh.map { it.title })
        coVerify { gameDao.upsertAll(match { it.size == 1 && it.first().id == "10" }) }

        every { query.get() } returns failureTask(RuntimeException("offline"))
        val fallback = repository.getGamesCatalog().getOrThrow()
        assertEquals(listOf("Old"), fallback.map { it.title })
    }

    @Test
    fun likedGamesAndPreferences_handleSuccessFallbackAndUnauthenticated() = runTest {
        every { userDoc.update("likedGames", any<FieldValue>()) } returns failureTask(RuntimeException("missing"))
        every { userDoc.set(any(), SetOptions.merge()) } returns successTask(mockk<Void>(relaxed = true))
        var ok = false
        repository.saveLikedGame("g1") { ok = it }
        assertTrue(ok)

        every { userDoc.update("likedGames", any<FieldValue>()) } returns successTask(mockk<Void>(relaxed = true))
        repository.removeLikedGame("g1") { ok = it }
        assertTrue(ok)

        val doc = mockk<DocumentSnapshot>(relaxed = true)
        every { doc.exists() } returns true
        every { doc.get("likedGames") } returns listOf("g1", 123)
        every { doc.toObject(User::class.java) } returns User(username = "Alex")
        every { userDoc.get() } returns successTask(doc)

        var likedRes: Result<List<String>>? = null
        repository.getLikedGames { likedRes = it }
        assertEquals(listOf("g1"), likedRes?.getOrNull())

        var prefsRes: Result<User>? = null
        repository.getUserPreferences { prefsRes = it }
        assertEquals("Alex", prefsRes?.getOrNull()?.username)

        every { auth.currentUser } returns null
        repository.getLikedGames { likedRes = it }
        assertTrue(likedRes!!.isFailure)
        repository.saveUserPreferences(User()) { ok = it }
        assertFalse(ok)
    }

    @Test
    fun reviewsCrud_handlesSuccessSortingAndFailure() {
        val doc1 = mockk<DocumentSnapshot>(relaxed = true)
        every { doc1.id } returns "r1"
        every { doc1.getString("comment") } returns "Older"
        every { doc1.getLong("rating") } returns 4L
        every { doc1.getLong("timestamp") } returns 100L
        val doc2 = mockk<DocumentSnapshot>(relaxed = true)
        every { doc2.id } returns "r2"
        every { doc2.getString("comment") } returns "Newer"
        every { doc2.getLong("rating") } returns 5L
        every { doc2.getLong("timestamp") } returns 200L

        val snapshot = mockk<QuerySnapshot>(relaxed = true)
        every { snapshot.documents } returns listOf(doc1, doc2)
        val query = mockk<Query>(relaxed = true)
        every { reviewsCol.whereEqualTo("gameId", "g1") } returns query
        every { query.get() } returns successTask(snapshot)

        var reviewsRes: Result<List<GameReview>>? = null
        repository.getReviewsForGame("g1") { reviewsRes = it }
        assertEquals(listOf("r2", "r1"), reviewsRes?.getOrNull()?.map { it.id })

        every { reviewsCol.add(any()) } returns successTask(mockk<DocumentReference>(relaxed = true))
        var ok = false
        repository.addReviewForGame(GameReview(gameId = "g1", comment = "Great")) { ok = it }
        assertTrue(ok)

        val reviewDoc = mockk<DocumentReference>(relaxed = true)
        every { reviewsCol.document("r1") } returns reviewDoc
        every { reviewDoc.update(any<Map<String, Any>>()) } returns successTask(mockk<Void>(relaxed = true))
        repository.updateReview("r1", 5, "Updated") { ok = it }
        assertTrue(ok)

        every { reviewDoc.delete() } returns failureTask(RuntimeException("denied"))
        repository.deleteReview("r1") { ok = it }
        assertFalse(ok)
    }
}
