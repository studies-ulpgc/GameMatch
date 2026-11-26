package com.pamn.gamematch.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pamn.gamematch.BuildConfig
import com.pamn.gamematch.data.local.GameDao
import com.pamn.gamematch.data.local.mapper.toEntity
import com.pamn.gamematch.data.model.Game
import com.pamn.gamematch.data.remote.RetrofitClient
import com.pamn.gamematch.data.remote.mapper.parseSteamBatchPrices
import com.pamn.gamematch.data.remote.mapper.parseSteamBatchWithoutPrice
import com.pamn.gamematch.data.remote.mapper.parseSteamIsFree
import com.pamn.gamematch.data.remote.mapper.toDomain
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

internal class IgdbCatalogSyncService(
    private val db: FirebaseFirestore,
    private val gameDao: GameDao?
) {

    suspend fun syncCatalog(collectionName: String, limit: Int): Result<Int> {
        if (BuildConfig.IGDB_CLIENT_ID.isBlank() || BuildConfig.IGDB_CLIENT_SECRET.isBlank()) {
            val err = "IGDB Client ID o Secret no están configurados en local.properties / BuildConfig"
            Log.w("GameRepository", err)
            return Result.failure(IllegalStateException(err))
        }

        return try {
            val domainGames = fetchIgdbDomainGames(limit)
            if (domainGames.isEmpty()) {
                return Result.failure(IllegalStateException("IGDB API no devolvió ningún juego"))
            }

            val steamPrices = fetchSteamPricesForGames(domainGames)
            val finalGames = domainGames.map { game ->
                val price = game.steamAppId?.let { steamPrices[it] } ?: game.price
                game.copy(price = price)
            }

            persistSyncedGames(collectionName, finalGames)
        } catch (e: Exception) {
            Log.e("GameRepository", "Error sincronizando catálogo con IGDB: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    private suspend fun fetchIgdbDomainGames(limit: Int): List<Game> {
        val nowSeconds = System.currentTimeMillis() / 1000L
        val oneYearAgoSeconds = nowSeconds - (365L * 24 * 3600)
        val commonFields = "fields name, summary, rating, rating_count, total_rating_count, follows, hypes, first_release_date, cover.image_id, screenshots.image_id, platforms.name, genres.name, external_games.external_game_source, external_games.category, external_games.uid, external_games.url"

        val queryA = "$commonFields; where cover != null & rating != null & rating_count >= 15 & platforms = (6) & external_games.external_game_source = (1); sort rating_count desc; limit $limit; offset 0;"
        val queryB = "$commonFields; where cover != null & rating != null & rating_count >= 3 & first_release_date >= $oneYearAgoSeconds & first_release_date <= $nowSeconds & platforms = (6) & external_games.external_game_source = (1); sort rating_count desc; limit 60; offset 0;"

        val dtosA = RetrofitClient.igdbApiService.getGames(queryA)
        val dtosB = try {
            RetrofitClient.igdbApiService.getGames(queryB)
        } catch (e: Exception) {
            Log.w("GameRepository", "Error consultando Query B de IGDB: ${e.localizedMessage}", e)
            emptyList()
        }

        return (dtosA + dtosB).distinctBy { it.id }.map { it.toDomain() }
    }

    private suspend fun fetchSteamPricesForGames(games: List<Game>): Map<Int, Double> {
        val steamPrices = mutableMapOf<Int, Double>()
        games.filter { it.steamAppId != null }.chunked(20).forEach { batchGames ->
            try {
                val appIdsStr = batchGames.mapNotNull { it.steamAppId }.joinToString(",")
                val steamResponse = RetrofitClient.steamApiService.getAppDetailsBatch(appIds = appIdsStr)
                steamPrices.putAll(parseSteamBatchPrices(steamResponse))

                for (appId in parseSteamBatchWithoutPrice(steamResponse)) {
                    try {
                        delay(300)
                        val basicResponse = RetrofitClient.steamApiService.getAppDetailsBasic(appIds = appId.toString())
                        if (parseSteamIsFree(basicResponse[appId.toString()])) {
                            steamPrices[appId] = 0.0
                        }
                    } catch (indivErr: Exception) {
                        Log.w("GameRepository", "Error consultando is_free individual para appId $appId: ${indivErr.localizedMessage}")
                    }
                }
            } catch (e: Exception) {
                Log.w("GameRepository", "Error al consultar lote de precios a Steam: ${e.localizedMessage}")
            }
        }
        return steamPrices
    }

    private suspend fun persistSyncedGames(collectionName: String, games: List<Game>): Result<Int> {
        var firestoreError: Exception? = null
        val firestoreSuccess = try {
            val batch = db.batch()
            games.forEach { game ->
                val docRef = db.collection(collectionName).document(game.id)
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
                game.ratingCount?.let { data["ratingCount"] = it }
                game.price?.let { data["price"] = it }
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            Log.i("GameRepository", "Sincronizados ${games.size} juegos de IGDB a Firestore.")
            true
        } catch (fsErr: Exception) {
            firestoreError = fsErr
            Log.w("GameRepository", "Fallo al guardar juegos IGDB en Firestore: ${fsErr.localizedMessage}")
            false
        }

        var roomError: Exception? = null
        val roomSuccess = try {
            gameDao?.upsertAll(games.map { it.toEntity() })
            Log.i("GameRepository", "Guardados ${games.size} juegos en Room.")
            true
        } catch (rErr: Exception) {
            roomError = rErr
            Log.w("GameRepository", "Fallo al guardar juegos en Room: ${rErr.localizedMessage}")
            false
        }

        return if (firestoreSuccess || roomSuccess) {
            Result.success(games.size)
        } else {
            Result.failure(firestoreError ?: roomError ?: IllegalStateException("Fallo al guardar en Firestore y en Room"))
        }
    }
}
