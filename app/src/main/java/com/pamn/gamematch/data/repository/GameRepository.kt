package com.pamn.gamematch.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pamn.gamematch.BuildConfig
import com.pamn.gamematch.data.local.GameDao
import com.pamn.gamematch.data.local.GameEntity
import com.pamn.gamematch.data.local.mapper.toDomain
import com.pamn.gamematch.data.local.mapper.toEntity
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.model.GameReview
import com.pamn.gamematch.data.model.User
import com.pamn.gamematch.data.remote.RetrofitClient
import com.pamn.gamematch.data.remote.mapper.parseSteamBatchPrices
import com.pamn.gamematch.data.remote.mapper.parseSteamIsFree
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

class GameRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val gameDao: GameDao? = null
) {

    companion object {
        val GAMES_COLLECTION: String get() = BuildConfig.GAMES_COLLECTION
        private const val CACHE_DURATION_MS = 24 * 3600 * 1000L
    }

    private val syncService by lazy { IgdbCatalogSyncService(db, gameDao) }

    suspend fun getSteamPrice(steamAppId: Int): Result<Double?> {
        return runCatching {
            val response = RetrofitClient.steamApiService.getAppDetailsBatch(appIds = steamAppId.toString())
            val pricesMap = parseSteamBatchPrices(response)
            if (pricesMap.containsKey(steamAppId)) {
                pricesMap[steamAppId]
            } else {
                delay(300)
                val basicResponse = RetrofitClient.steamApiService.getAppDetailsBasic(appIds = steamAppId.toString())
                if (parseSteamIsFree(basicResponse[steamAppId.toString()])) 0.0 else null
            }
        }.onFailure { e ->
            Log.w("GameRepository", "Error consultando precio en Steam para appId $steamAppId: ${e.localizedMessage}")
        }
    }

    suspend fun getGamesCatalog(
        page: Int = 1,
        pageSize: Int = 300,
        searchQuery: String? = null,
        genres: String? = null
    ): Result<List<Game>> {
        return try {
            val localEntities = readLocalCacheSafe()
            val isCacheValid = localEntities.isNotEmpty() &&
                (System.currentTimeMillis() - localEntities.maxOf { it.cachedAt }) <= CACHE_DURATION_MS

            var catalogGames = if (isCacheValid) {
                localEntities.map { it.toDomain() }
            } else {
                loadRemoteOrFallbackCatalog(localEntities, pageSize)
            }

            if (!searchQuery.isNullOrBlank()) {
                val q = searchQuery.trim()
                catalogGames = catalogGames.filter {
                    it.title.contains(q, ignoreCase = true) || it.genre.contains(q, ignoreCase = true)
                }
            }
            if (!genres.isNullOrBlank()) {
                val g = genres.trim()
                catalogGames = catalogGames.filter { it.genre.contains(g, ignoreCase = true) }
            }

            Result.success(catalogGames.take(pageSize))
        } catch (e: Exception) {
            Log.e("GameRepository", "Error general en getGamesCatalog: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    private suspend fun readLocalCacheSafe(): List<GameEntity> {
        return try {
            gameDao?.getAll() ?: emptyList()
        } catch (e: Exception) {
            Log.w("GameRepository", "Error al consultar la BD local de Room: ${e.localizedMessage}")
            emptyList()
        }
    }

    private suspend fun loadRemoteOrFallbackCatalog(
        localEntities: List<GameEntity>,
        pageSize: Int
    ): List<Game> {
        val firestoreGames = fetchFirestoreCatalogSafe(pageSize)
        if (firestoreGames.isNotEmpty()) {
            upsertLocalSafe(firestoreGames)
            return firestoreGames
        }
        if (localEntities.isNotEmpty()) {
            return localEntities.map { it.toDomain() }
        }
        val syncResult = syncCatalogFromIgdb(limit = pageSize)
        if (!syncResult.isSuccess) return emptyList()

        val syncedGames = fetchFirestoreCatalogSafe(pageSize)
        if (syncedGames.isNotEmpty()) {
            upsertLocalSafe(syncedGames)
            return syncedGames
        }
        return readLocalCacheSafe().map { it.toDomain() }
    }

    private suspend fun fetchFirestoreCatalogSafe(pageSize: Int): List<Game> {
        return try {
            val snapshot = db.collection(GAMES_COLLECTION).limit(pageSize.toLong()).get().await()
            parseGamesFromSnapshot(snapshot)
        } catch (e: Exception) {
            Log.w("GameRepository", "Fallo al consultar Firestore: ${e.localizedMessage}")
            emptyList()
        }
    }

    private suspend fun upsertLocalSafe(games: List<Game>) {
        try {
            gameDao?.upsertAll(games.map { it.toEntity() })
        } catch (e: Exception) {
            Log.w("GameRepository", "Error guardando catálogo en Room: ${e.localizedMessage}")
        }
    }

    suspend fun syncCatalogFromIgdb(limit: Int = 300): Result<Int> =
        syncService.syncCatalog(GAMES_COLLECTION, limit)

    fun saveLikedGame(gameId: String, onResult: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onResult(false)
        db.collection("users").document(userId)
            .update("likedGames", FieldValue.arrayUnion(gameId))
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener {
                db.collection("users").document(userId)
                    .set(hashMapOf("likedGames" to listOf(gameId)), SetOptions.merge())
                    .addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { onResult(false) }
            }
    }

    fun removeLikedGame(gameId: String, onResult: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onResult(false)
        db.collection("users").document(userId)
            .update("likedGames", FieldValue.arrayRemove(gameId))
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getLikedGames(onResult: (Result<List<String>>) -> Unit) {
        val userId = auth.currentUser?.uid
            ?: return onResult(Result.failure(IllegalStateException("Usuario no autenticado")))

        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                val ids = (doc.get("likedGames") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                onResult(Result.success(ids))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun getUserPreferences(onResult: (Result<User>) -> Unit) {
        val userId = auth.currentUser?.uid
            ?: return onResult(Result.failure(IllegalStateException("Usuario no autenticado")))

        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                val user = if (doc != null && doc.exists()) doc.toObject(User::class.java) ?: User() else User()
                onResult(Result.success(user))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun saveUserPreferences(userModel: User, onResult: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return onResult(false)
        db.collection("users").document(currentUser.uid)
            .set(userModel, SetOptions.merge())
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getReviewsForGame(gameId: String, onResult: (Result<List<GameReview>>) -> Unit) {
        db.collection("reviews").whereEqualTo("gameId", gameId).get()
            .addOnSuccessListener { querySnapshot ->
                val reviews = querySnapshot.documents.map { doc ->
                    GameReview(
                        id = doc.id,
                        gameId = doc.getString("gameId") ?: "",
                        gameTitle = doc.getString("gameTitle") ?: "",
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "Jugador",
                        rating = (doc.getLong("rating") ?: 5L).toInt(),
                        comment = doc.getString("comment") ?: "",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    )
                }.sortedByDescending { it.timestamp }
                onResult(Result.success(reviews))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun addReviewForGame(review: GameReview, onResult: (Boolean) -> Unit) {
        val data = hashMapOf(
            "gameId" to review.gameId,
            "gameTitle" to review.gameTitle,
            "userId" to review.userId,
            "userName" to review.userName,
            "rating" to review.rating,
            "comment" to review.comment,
            "timestamp" to review.timestamp
        )
        db.collection("reviews").add(data)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun updateReview(reviewId: String, rating: Int, comment: String, onResult: (Boolean) -> Unit) {
        val updates = mapOf(
            "rating" to rating,
            "comment" to comment,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("reviews").document(reviewId).update(updates)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun deleteReview(reviewId: String, onResult: (Boolean) -> Unit) {
        db.collection("reviews").document(reviewId).delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}
