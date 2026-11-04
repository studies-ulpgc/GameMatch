package com.pamn.gamematch.data.remote.api

import com.pamn.gamematch.data.remote.dto.steam.SteamAppContainerDto
import retrofit2.http.GET
import retrofit2.http.Query

interface SteamApiService {
    @GET("api/appdetails")
    suspend fun getAppDetails(
        @Query("appids") appIds: Int,
        @Query("cc") countryCode: String = "es",
        @Query("filters") filters: String = "price_overview"
    ): Map<String, SteamAppContainerDto>
}