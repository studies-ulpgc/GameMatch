package com.pamn.gamematch.data.remote.api

import com.pamn.gamematch.data.remote.dto.igdb.TwitchTokenResponseDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TwitchAuthApiService {
    @FormUrlEncoded
    @POST("oauth2/token")
    suspend fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): TwitchTokenResponseDto
}
