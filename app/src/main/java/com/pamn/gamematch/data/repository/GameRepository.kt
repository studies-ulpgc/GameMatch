package com.pamn.gamematch.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pamn.gamematch.BuildConfig
import com.pamn.gamematch.data.local.GameDao
import com.pamn.gamematch.data.local.mapper.toDomain
import com.pamn.gamematch.data.local.mapper.toEntity
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.model.GameReview
import com.pamn.gamematch.data.model.User
import com.pamn.gamematch.data.remote.RetrofitClient
import com.pamn.gamematch.data.remote.mapper.toDomain
import kotlinx.coroutines.tasks.await

class GameRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val gameDao: GameDao?
        get() = try {
            com.pamn.gamematch.GameMatchApp.getDatabase()?.gameDao()
        } catch (_: Exception) {
            null
        }

    companion object {
        const val GAMES_COLLECTION = "games_test"
    }

    /**
     * Consulta el precio de un juego en la tienda pública de Steam bajo demanda.
     */
    suspend fun getSteamPrice(steamAppId: Int): Result<Double?> {
        return runCatching {
            val response = RetrofitClient.steamApiService.getAppDetailsBatch(appIds = steamAppId.toString())
            val pricesMap = com.pamn.gamematch.data.remote.mapper.parseSteamBatchPrices(response)
            if (pricesMap.containsKey(steamAppId)) {
                pricesMap[steamAppId]
            } else {
                kotlinx.coroutines.delay(300)
                val basicResponse = RetrofitClient.steamApiService.getAppDetailsBasic(appIds = steamAppId.toString())
                val element = basicResponse[steamAppId.toString()]
                if (com.pamn.gamematch.data.remote.mapper.parseSteamIsFree(element)) {
                    0.0
                } else {
                    null
                }
            }
        }.onFailure { e ->
            Log.w("GameRepository", "Error consultando precio en Steam para appId $steamAppId: ${e.localizedMessage}")
        }
    }

    private fun parseGamesFromSnapshot(snapshot: com.google.firebase.firestore.QuerySnapshot): List<Game> {
        return snapshot.documents.mapNotNull { doc ->
            val id = doc.id
            val title = doc.getString("title") ?: doc.getString("name") ?: ""
            val description = doc.getString("description") ?: ""
            val imageUrl = doc.getString("imageUrl") ?: doc.getString("background_image") ?: ""
            val genre = doc.getString("genre") ?: ""
            val price = doc.getDouble("price")
            val releaseDate = doc.getString("releaseDate") ?: "Disponible"
            val isReleased = doc.getBoolean("isReleased") ?: true
            val platforms = (doc.get("platforms") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            val steamAppId = doc.getLong("steamAppId")?.toInt()
            val rating = doc.getDouble("rating") ?: 0.0
            val ratingCount = doc.getLong("ratingCount")?.toInt()
            val isFeatured = doc.getBoolean("isFeatured") ?: false
            val isTrending = doc.getBoolean("isTrending") ?: false

            if (title.isNotBlank()) {
                Game(
                    id = id,
                    title = title,
                    description = description,
                    imageUrl = imageUrl,
                    genre = genre,
                    price = price,
                    releaseDate = releaseDate,
                    isReleased = isReleased,
                    platforms = platforms,
                    steamAppId = steamAppId,
                    rating = rating,
                    ratingCount = ratingCount,
                    isFeatured = isFeatured,
                    isTrending = isTrending
                )
            } else null
        }
    }

    /**
     * Consulta el catálogo de juegos utilizando caché local (Room) con estrategia de caducidad (24h),
     * refrescando desde Cloud Firestore e IGDB cuando sea necesario.
     */
    suspend fun getGamesCatalog(
        page: Int = 1,
        pageSize: Int = 300,
        searchQuery: String? = null,
        genres: String? = null
    ): Result<List<Game>> {
        return try {
            val now = System.currentTimeMillis()
            val cacheDurationMs = 24 * 3600 * 1000L

            var localEntities: List<com.pamn.gamematch.data.local.GameEntity> = emptyList()
            try {
                localEntities = gameDao?.getAll() ?: emptyList()
            } catch (e: Exception) {
                Log.w("GameRepository", "Error al consultar la BD local de Room: ${e.localizedMessage}")
            }

            val isCacheValid = localEntities.isNotEmpty() && (now - localEntities.maxOf { it.cachedAt }) <= cacheDurationMs

            var catalogGames: List<Game> = if (isCacheValid) {
                Log.i("GameRepository", "Cargados ${localEntities.size} juegos desde la caché local de Room (válida).")
                localEntities.map { it.toDomain() }
            } else {
                var firestoreGames: List<Game> = emptyList()
                try {
                    val snapshot = db.collection(GAMES_COLLECTION).limit(pageSize.toLong()).get().await()
                    firestoreGames = parseGamesFromSnapshot(snapshot)
                } catch (fsErr: Exception) {
                    Log.w("GameRepository", "Fallo al consultar Firestore durante el refresco: ${fsErr.localizedMessage}")
                }

                if (firestoreGames.isNotEmpty()) {
                    Log.i("GameRepository", "Cargados ${firestoreGames.size} juegos desde Firestore. Actualizando Room...")
                    try {
                        gameDao?.upsertAll(firestoreGames.map { it.toEntity() })
                    } catch (roomErr: Exception) {
                        Log.w("GameRepository", "Error guardando catálogo en Room tras leer de Firestore: ${roomErr.localizedMessage}")
                    }
                    firestoreGames
                } else if (localEntities.isNotEmpty()) {
                    Log.i("GameRepository", "Firestore no disponible/vacío. Usando ${localEntities.size} juegos de Room como fallback.")
                    localEntities.map { it.toDomain() }
                } else {
                    Log.i("GameRepository", "Sin datos en Room ni Firestore. Ejecutando sincronización desde IGDB...")
                    val syncResult = syncCatalogFromIgdb(limit = pageSize)
                    if (syncResult.isSuccess) {
                        var syncedGames = emptyList<Game>()
                        try {
                            val newSnapshot = db.collection(GAMES_COLLECTION).limit(pageSize.toLong()).get().await()
                            syncedGames = parseGamesFromSnapshot(newSnapshot)
                            gameDao?.upsertAll(syncedGames.map { it.toEntity() })
                        } catch (syncErr: Exception) {
                            Log.w("GameRepository", "Error al leer Firestore/Room tras sincronización desde IGDB: ${syncErr.localizedMessage}")
                        }
                        if (syncedGames.isEmpty()) {
                            syncedGames = gameDao?.getAll()?.map { it.toDomain() } ?: emptyList()
                        }
                        syncedGames
                    } else {
                        emptyList()
                    }
                }
            }

            if (!searchQuery.isNullOrBlank()) {
                val q = searchQuery.trim()
                catalogGames = catalogGames.filter {
                    it.title.contains(q, ignoreCase = true) || it.genre.contains(q, ignoreCase = true)
                }
            }

            if (!genres.isNullOrBlank()) {
                val g = genres.trim()
                catalogGames = catalogGames.filter {
                    it.genre.contains(g, ignoreCase = true)
                }
            }

            val paginated = catalogGames.take(pageSize)
            Result.success(paginated)
        } catch (e: Exception) {
            Log.e("GameRepository", "Error general en getGamesCatalog: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    /**
     * Sincronización aislada desde IGDB API para alimentar la colección de juegos en Cloud Firestore y Room.
     */
    suspend fun syncCatalogFromIgdb(limit: Int = 300): Result<Int> {
        val clientId = BuildConfig.IGDB_CLIENT_ID
        val clientSecret = BuildConfig.IGDB_CLIENT_SECRET
        if (clientId.isBlank() || clientSecret.isBlank()) {
            val err = "IGDB Client ID o Secret no están configurados en local.properties / BuildConfig"
            Log.w("GameRepository", err)
            return Result.failure(IllegalStateException(err))
        }

        return try {
            val nowSeconds = System.currentTimeMillis() / 1000L
            val oneYearAgoSeconds = nowSeconds - (365L * 24 * 3600)

            val queryA = "fields name, summary, rating, rating_count, total_rating_count, follows, hypes, first_release_date, cover.image_id, screenshots.image_id, platforms.name, genres.name, external_games.external_game_source, external_games.category, external_games.uid, external_games.url; where cover != null & rating != null & rating_count >= 15 & platforms = (6) & external_games.external_game_source = (1); sort rating_count desc; limit $limit; offset 0;"
            val queryB = "fields name, summary, rating, rating_count, total_rating_count, follows, hypes, first_release_date, cover.image_id, screenshots.image_id, platforms.name, genres.name, external_games.external_game_source, external_games.category, external_games.uid, external_games.url; where cover != null & rating != null & rating_count >= 3 & first_release_date >= $oneYearAgoSeconds & first_release_date <= $nowSeconds & platforms = (6) & external_games.external_game_source = (1); sort rating_count desc; limit 60; offset 0;"

            val dtosA = RetrofitClient.igdbApiService.getGames(queryA)
            val dtosB = try {
                RetrofitClient.igdbApiService.getGames(queryB)
            } catch (e: Exception) {
                Log.w("GameRepository", "Error consultando Query B de IGDB: ${e.localizedMessage}", e)
                emptyList()
            }

            val combinedDtos = (dtosA + dtosB).distinctBy { it.id }
            val domainGames = combinedDtos.map { it.toDomain() }

            if (domainGames.isEmpty()) {
                return Result.failure(IllegalStateException("IGDB API no devolvió ningún juego"))
            }

            // Consultar precios en Steam por lotes de 20 para no saturar la API
            val gamesWithSteamId = domainGames.filter { it.steamAppId != null }
            val steamPrices = mutableMapOf<Int, Double>()

            gamesWithSteamId.chunked(20).forEach { batchGames ->
                try {
                    val appIdsStr = batchGames.map { it.steamAppId }.joinToString(",")
                    val steamResponse = RetrofitClient.steamApiService.getAppDetailsBatch(appIds = appIdsStr)

                    // 1. Obtener precios normales de pago
                    steamPrices.putAll(com.pamn.gamematch.data.remote.mapper.parseSteamBatchPrices(steamResponse))

                    // 2. Obtener appids sin precio (por ejemplo F2P con data: [])
                    val noPriceAppIds = com.pamn.gamematch.data.remote.mapper.parseSteamBatchWithoutPrice(steamResponse)

                    for (appId in noPriceAppIds) {
                        try {
                            kotlinx.coroutines.delay(300)
                            val basicResponse = RetrofitClient.steamApiService.getAppDetailsBasic(appIds = appId.toString())
                            val element = basicResponse[appId.toString()]
                            if (com.pamn.gamematch.data.remote.mapper.parseSteamIsFree(element)) {
                                steamPrices[appId] = 0.0
                            }
                        } catch (indivErr: Exception) {
                            Log.w("GameRepository", "Error consultando is_free individual para appId $appId: ${indivErr.localizedMessage}")
                        }
                    }
                } catch (e: Exception) {
                    Log.w("GameRepository", "Error al consultar lote de precios a Steam (no se bloqueará la sincronización): ${e.localizedMessage}")
                }
            }

            val finalDomainGames = domainGames.map { game ->
                val price = if (game.steamAppId != null && steamPrices.containsKey(game.steamAppId)) {
                    steamPrices[game.steamAppId]
                } else {
                    game.price
                }
                game.copy(price = price)
            }

            var firestoreSuccess = false
            var firestoreError: Exception? = null

            try {
                val batch = db.batch()
                finalDomainGames.forEach { game ->
                    val docRef = db.collection(GAMES_COLLECTION).document(game.id)
                    val data = hashMapOf<String, Any?>(
                        "title" to game.title,
                        "description" to game.description,
                        "imageUrl" to game.imageUrl,
                        "genre" to game.genre,
                        "releaseDate" to game.releaseDate,
                        "isReleased" to game.isReleased,
                        "platforms" to game.platforms,
                        "steamAppId" to game.steamAppId,
                        "rating" to game.rating,
                        "isFeatured" to game.isFeatured,
                        "isTrending" to game.isTrending,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    if (game.ratingCount != null) {
                        data["ratingCount"] = game.ratingCount
                    }
                    if (game.price != null) {
                        data["price"] = game.price
                    }

                    batch.set(docRef, data, SetOptions.merge())
                }
                batch.commit().await()
                firestoreSuccess = true
                Log.i("GameRepository", "Sincronizados ${finalDomainGames.size} juegos de IGDB a Firestore.")
            } catch (fsErr: Exception) {
                firestoreError = fsErr
                Log.w("GameRepository", "Petición IGDB exitosa (${finalDomainGames.size} juegos), pero fallo al guardar en Firestore: ${fsErr.localizedMessage}")
            }

            var roomSuccess = false
            var roomError: Exception? = null

            try {
                gameDao?.upsertAll(finalDomainGames.map { it.toEntity() })
                roomSuccess = true
                Log.i("GameRepository", "Guardados ${finalDomainGames.size} juegos en la base de datos local de Room.")
            } catch (rErr: Exception) {
                roomError = rErr
                Log.w("GameRepository", "Fallo al guardar juegos en la base de datos local de Room: ${rErr.localizedMessage}")
            }

            if (firestoreSuccess || roomSuccess) {
                Result.success(finalDomainGames.size)
            } else {
                val primaryErr = firestoreError ?: roomError ?: IllegalStateException("Fallo al guardar en Firestore y en Room")
                Result.failure(primaryErr)
            }
        } catch (e: Exception) {
            Log.e("GameRepository", "Error sincronizando catálogo con IGDB: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    /**
     * Método auxiliar de apoyo exclusivamente para previsualizaciones (@Preview) en Jetpack Compose.
     */
    fun getPreviewGames(): List<Game> {
        return listOf(
            Game(
                id = "1",
                title = "The Legend of Zelda: Tears of the Kingdom",
                description = "Aventura épica de exploración e investigación en un vasto mundo abierto sobre las tierras y los cielos de Hyrule.",
                imageUrl = "https://cdn.cloudflare.steamstatic.com/steam/apps/1245620/header.jpg",
                genre = "Aventura",
                price = 59.99,
                releaseDate = "Disponible",
                isReleased = true,
                platforms = listOf("Nintendo"),
                isFeatured = true,
                isTrending = false
            ),
            Game(
                id = "2",
                title = "GTA VI",
                description = "Siguiente entrega de la aclamada franquicia de acción y crimen en un impresionante mundo abierto hiperrealista.",
                imageUrl = "https://cdn.cloudflare.steamstatic.com/steam/apps/1091500/header.jpg",
                genre = "Acción",
                price = 79.99,
                releaseDate = "Lanzamiento: Otoño 2026",
                isReleased = false,
                platforms = listOf("PlayStation", "Xbox"),
                isFeatured = true,
                isTrending = true
            )
        )
    }

    fun saveLikedGame(gameId: String, onResult: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onResult(false)

        db.collection("users").document(userId)
            .update("likedGames", FieldValue.arrayUnion(gameId))
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener {
                val userData = hashMapOf("likedGames" to listOf(gameId))
                db.collection("users").document(userId)
                    .set(userData, SetOptions.merge())
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

    /**
     * Lee el array "likedGames" del documento del usuario autenticado.
     * Devuelve lista vacía si el documento o el campo aún no existen.
     */
    fun getLikedGames(onResult: (Result<List<String>>) -> Unit) {
        val userId = auth.currentUser?.uid
            ?: return onResult(Result.failure(IllegalStateException("Usuario no autenticado")))

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val ids = (doc.get("likedGames") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?: emptyList()
                onResult(Result.success(ids))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun getUserPreferences(onResult: (Result<User>) -> Unit) {
        val userId = auth.currentUser?.uid
            ?: return onResult(Result.failure(IllegalStateException("Usuario no autenticado")))

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val user = doc.toObject(User::class.java) ?: User()
                    onResult(Result.success(user))
                } else {
                    onResult(Result.success(User()))
                }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun saveUserPreferences(
        userModel: User,
        onResult: (Boolean) -> Unit
    ) {
        val currentUser = auth.currentUser ?: return onResult(false)

        db.collection("users").document(currentUser.uid)
            .set(userModel, SetOptions.merge())
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    /**
     * Consulta las reseñas de la colección raíz 'reviews' asociadas a un juego específico.
     */
    fun getReviewsForGame(gameId: String, onResult: (Result<List<GameReview>>) -> Unit) {
        db.collection("reviews")
            .whereEqualTo("gameId", gameId)
            .get()
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

    /**
     * Añade una nueva reseña a la colección 'reviews'.
     */
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

    /**
     * Actualiza una reseña existente en la colección 'reviews'.
     */
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

    /**
     * Elimina una reseña existente de la colección 'reviews'.
     */
    fun deleteReview(reviewId: String, onResult: (Boolean) -> Unit) {
        db.collection("reviews").document(reviewId).delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}
