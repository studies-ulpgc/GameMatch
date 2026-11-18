package com.pamn.gamematch.data.local.mapper

import com.pamn.gamematch.data.local.GameEntity
import com.pamn.gamematch.data.model.Game

fun GameEntity.toDomain(): Game {
    return Game(
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
}

fun Game.toEntity(cachedAt: Long = System.currentTimeMillis()): GameEntity {
    return GameEntity(
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
        isTrending = isTrending,
        cachedAt = cachedAt
    )
}
