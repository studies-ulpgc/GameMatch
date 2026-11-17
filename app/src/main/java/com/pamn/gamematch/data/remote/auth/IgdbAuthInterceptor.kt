package com.pamn.gamematch.data.remote.auth

import com.pamn.gamematch.BuildConfig
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class IgdbAuthInterceptor(
    private val tokenProvider: IgdbTokenProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenProvider.getAccessToken(forceRefresh = false) }
        val request = chain.request().newBuilder()
            .header("Client-ID", BuildConfig.IGDB_CLIENT_ID)
            .header("Authorization", "Bearer $token")
            .build()

        var response = chain.proceed(request)

        if (response.code == 401) {
            response.close()
            val newToken = runBlocking { tokenProvider.getAccessToken(forceRefresh = true) }
            val newRequest = chain.request().newBuilder()
                .header("Client-ID", BuildConfig.IGDB_CLIENT_ID)
                .header("Authorization", "Bearer $newToken")
                .build()
            response = chain.proceed(newRequest)
        }

        return response
    }
}
