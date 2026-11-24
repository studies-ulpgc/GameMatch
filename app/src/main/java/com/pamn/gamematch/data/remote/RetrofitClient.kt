package com.pamn.gamematch.data.remote

import com.pamn.gamematch.data.remote.api.IgdbApiService
import com.pamn.gamematch.data.remote.api.SteamApiService
import com.pamn.gamematch.data.remote.api.TwitchAuthApiService
import com.pamn.gamematch.data.remote.auth.IgdbAuthInterceptor
import com.pamn.gamematch.data.remote.auth.IgdbTokenProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.pamn.gamematch.BuildConfig
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {
    private const val TWITCH_AUTH_BASE_URL = "https://id.twitch.tv/"
    private const val IGDB_BASE_URL = "https://api.igdb.com/"
    private const val STEAM_BASE_URL = "https://store.steampowered.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    val twitchAuthApiService: TwitchAuthApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(TWITCH_AUTH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TwitchAuthApiService::class.java)
    }

    val tokenProvider: IgdbTokenProvider by lazy {
        IgdbTokenProvider(twitchAuthApiService)
    }

    val igdbApiService: IgdbApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(IgdbAuthInterceptor(tokenProvider))
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(IGDB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IgdbApiService::class.java)
    }

    val steamApiService: SteamApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(STEAM_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SteamApiService::class.java)
    }
}
