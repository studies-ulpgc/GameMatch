package com.pamn.gamematch.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pamn.gamematch.BuildConfig
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.model.GameReview
import com.pamn.gamematch.data.model.User
import com.pamn.gamematch.data.remote.RetrofitClient
import com.pamn.gamematch.data.remote.mapper.toDomain
import kotlinx.coroutines.tasks.await

class GameRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        const val GAMES_COLLECTION = "games_test"
    }

    /**
     * Consulta el precio formateado de un juego en la tienda pública de Steam bajo demanda.
     */
    suspend fun getSteamPrice(steamAppId: Int): Result<String?> {
        return runCatching {
            val response = RetrofitClient.steamApiService.getAppDetails(appIds = steamAppId)
            val appContainer = response[steamAppId.toString()]
            if (appContainer?.success == true) {
                val appData = appContainer.data
                if (appData?.isFree == true) {
                    "Gratis"
                } else {
                    appData?.priceOverview?.finalFormatted
                }
            } else {
                null
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
                    isFeatured = isFeatured,
                    isTrending = isTrending
                )
            } else null
        }
    }

    /**
     * Consulta el catálogo de juegos exclusivamente desde la colección de juegos en Cloud Firestore.
     * Si la colección está vacía en Firestore, realiza una auto-sincronización inicial desde IGDB API.
     */
    suspend fun getGamesCatalog(
        page: Int = 1,
        pageSize: Int = 50,
        searchQuery: String? = null,
        genres: String? = null
    ): Result<List<Game>> {
        return try {
            val snapshot = db.collection(GAMES_COLLECTION).limit(pageSize.toLong()).get().await()
            var cachedGames = parseGamesFromSnapshot(snapshot)

            // Si la colección de juegos está vacía en Firestore, auto-sincronizar desde IGDB
            if (cachedGames.isEmpty() && searchQuery.isNullOrBlank() && genres.isNullOrBlank()) {
                Log.i("GameRepository", "La colección '$GAMES_COLLECTION' en Firestore está vacía. Sincronizando automáticamente desde IGDB...")
                val syncResult = syncCatalogFromIgdb(limit = pageSize)
                if (syncResult.isSuccess) {
                    val newSnapshot = db.collection(GAMES_COLLECTION).limit(pageSize.toLong()).get().await()
                    cachedGames = parseGamesFromSnapshot(newSnapshot)
                }
            }

            if (!searchQuery.isNullOrBlank()) {
                val q = searchQuery.trim()
                cachedGames = cachedGames.filter {
                    it.title.contains(q, ignoreCase = true) || it.genre.contains(q, ignoreCase = true)
                }
            }

            if (!genres.isNullOrBlank()) {
                val g = genres.trim()
                cachedGames = cachedGames.filter {
                    it.genre.contains(g, ignoreCase = true)
                }
            }

            if (cachedGames.isNotEmpty()) {
                Log.i("GameRepository", "Cargados ${cachedGames.size} juegos desde Firestore.")
                Result.success(cachedGames)
            } else {
                Log.i("GameRepository", "La colección '$GAMES_COLLECTION' en Firestore está vacía.")
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Log.e("GameRepository", "Error al consultar la colección '$GAMES_COLLECTION' en Firestore: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    /**
     * Sincronización aislada desde IGDB API para alimentar la colección de juegos en Cloud Firestore.
     */
    suspend fun syncCatalogFromIgdb(limit: Int = 40): Result<Int> {
        val clientId = BuildConfig.IGDB_CLIENT_ID
        val clientSecret = BuildConfig.IGDB_CLIENT_SECRET
        if (clientId.isBlank() || clientSecret.isBlank()) {
            val err = "IGDB Client ID o Secret no están configurados en local.properties / BuildConfig"
            Log.w("GameRepository", err)
            return Result.failure(IllegalStateException(err))
        }

        return try {
            val query = "fields name, summary, rating, genres.name, platforms.name, cover.image_id, screenshots.image_id, first_release_date, external_games.external_game_source, external_games.category, external_games.uid, external_games.url; where cover != null & rating != null; sort rating desc; limit $limit; offset 0;"
            val igdbDtos = RetrofitClient.igdbApiService.getGames(query)
            val domainGames = igdbDtos.map { it.toDomain() }

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
                    steamPrices.putAll(com.pamn.gamematch.data.remote.mapper.parseSteamBatchPrices(steamResponse))
                } catch (e: Exception) {
                    Log.w("GameRepository", "Error al consultar lote de precios a Steam (no se bloqueará la sincronización): ${e.localizedMessage}")
                }
            }

            try {
                val batch = db.batch()
                domainGames.forEach { game ->
                    val docRef = db.collection(GAMES_COLLECTION).document(game.id)
                    val data = hashMapOf(
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
                    
                    val finalPrice = if (game.steamAppId != null && steamPrices.containsKey(game.steamAppId)) {
                        steamPrices[game.steamAppId]
                    } else {
                        game.price
                    }
                    if (finalPrice != null) {
                        data["price"] = finalPrice
                    }

                    batch.set(docRef, data, SetOptions.merge())
                }
                batch.commit().await()
                Log.i("GameRepository", "Sincronizados ${domainGames.size} juegos de IGDB a Firestore.")
            } catch (fsErr: Exception) {
                Log.w("GameRepository", "Petición IGDB exitosa (${domainGames.size} juegos), pero fallo al guardar en Firestore (¿permisos?): ${fsErr.localizedMessage}")
            }

            Result.success(domainGames.size)
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