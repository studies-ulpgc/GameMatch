package com.pamn.gamematch.data.remote.api

import com.pamn.gamematch.data.remote.dto.rawg.RawgGameResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface RawgApiService {
    @GET("games")
    suspend fun getGames(
        @Query("key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("search") search: String? = null,
        @Query("genres") genres: String? = null,
        @Query("ordering") ordering: String? = null
    ): RawgGameResponseDto
}