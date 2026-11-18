package com.pamn.gamematch.data.remote.api

import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Query

interface SteamApiService {
    @GET("api/appdetails")
    suspend fun getAppDetailsBatch(
        @Query("appids") appIds: String,
        @Query("cc") countryCode: String = "es",
        @Query("filters") filters: String = "price_overview"
    ): Map<String, JsonElement>

    @GET("api/appdetails")
    suspend fun getAppDetailsBasic(
        @Query("appids") appIds: String,
        @Query("cc") countryCode: String = "es",
        @Query("filters") filters: String = "basic"
    ): Map<String, JsonElement>
}
