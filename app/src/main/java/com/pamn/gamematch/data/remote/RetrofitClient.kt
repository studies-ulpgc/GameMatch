package com.pamn.gamematch.data.remote

import com.pamn.gamematch.data.remote.api.RawgApiService
import com.pamn.gamematch.data.remote.api.SteamApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val RAWG_BASE_URL = "https://api.rawg.io/api/"
    private const val STEAM_BASE_URL = "https://store.steampowered.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val rawgApiService: RawgApiService by lazy {
        Retrofit.Builder()
            .baseUrl(RAWG_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RawgApiService::class.java)
    }

    val steamApiService: SteamApiService by lazy {
        Retrofit.Builder()
            .baseUrl(STEAM_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SteamApiService::class.java)
    }
}