package com.pamn.gamematch.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val genre: String,
    val price: Double?,
    val releaseDate: String,
    val isReleased: Boolean,
    val platforms: List<String>,
    val steamAppId: Int?,
    val rating: Double,
    val ratingCount: Int?,
    val isFeatured: Boolean,
    val isTrending: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)
