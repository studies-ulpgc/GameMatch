package com.pamn.gamematch.data.remote.auth

import com.pamn.gamematch.BuildConfig
import com.pamn.gamematch.data.remote.api.TwitchAuthApiService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class IgdbTokenProvider(
    private val authApiService: TwitchAuthApiService
) {
    private var cachedAccessToken: String? = null
    private var expiresAtMillis: Long = 0L
    private val mutex = Mutex()

    suspend fun getAccessToken(forceRefresh: Boolean = false): String {
        return mutex.withLock {
            val now = System.currentTimeMillis()
            if (!forceRefresh && cachedAccessToken != null && now < expiresAtMillis - 60_000) {
                return@withLock cachedAccessToken!!
            }

            val clientId = BuildConfig.IGDB_CLIENT_ID
            val clientSecret = BuildConfig.IGDB_CLIENT_SECRET

            if (clientId.isBlank() || clientSecret.isBlank()) {
                throw IllegalStateException("IGDB_CLIENT_ID o IGDB_CLIENT_SECRET no están configurados en local.properties")
            }

            val response = authApiService.getAccessToken(
                clientId = clientId,
                clientSecret = clientSecret
            )

            cachedAccessToken = response.accessToken
            expiresAtMillis = System.currentTimeMillis() + (response.expiresIn * 1000L)
            response.accessToken
        }
    }
}
