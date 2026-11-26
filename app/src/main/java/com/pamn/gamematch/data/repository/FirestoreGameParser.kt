package com.pamn.gamematch.data.repository

import com.google.firebase.firestore.QuerySnapshot
import com.pamn.gamematch.data.model.Game

internal fun parseGamesFromSnapshot(snapshot: QuerySnapshot): List<Game> {
    return snapshot.documents.mapNotNull { doc ->
        val title = doc.getString("title") ?: doc.getString("name") ?: ""
        if (title.isBlank()) return@mapNotNull null

        Game(
            id = doc.id,
            title = title,
            description = doc.getString("description") ?: "",
            imageUrl = doc.getString("imageUrl") ?: doc.getString("background_image") ?: "",
            genre = doc.getString("genre") ?: "",
            price = doc.getDouble("price"),
            releaseDate = doc.getString("releaseDate") ?: "Disponible",
            isReleased = doc.getBoolean("isReleased") ?: true,
            platforms = (doc.get("platforms") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            steamAppId = doc.getLong("steamAppId")?.toInt(),
            rating = doc.getDouble("rating") ?: 0.0,
            ratingCount = doc.getLong("ratingCount")?.toInt(),
            isFeatured = doc.getBoolean("isFeatured") ?: false,
            isTrending = doc.getBoolean("isTrending") ?: false
        )
    }
}
