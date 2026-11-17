package com.pamn.gamematch.data.remote.api

import com.pamn.gamematch.data.remote.dto.igdb.IgdbGameDto
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IgdbApiService {
    @Headers("Content-Type: text/plain")
    @POST("v4/games")
    suspend fun getGames(
        @Body query: String
    ): List<IgdbGameDto>
}
