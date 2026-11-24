package com.pamn.gamematch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GameDao {
    @Query("SELECT * FROM games")
    suspend fun getAll(): List<GameEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(games: List<GameEntity>)

    @Query("SELECT COUNT(*) FROM games")
    suspend fun count(): Int

    @Query("DELETE FROM games")
    suspend fun clear()
}
