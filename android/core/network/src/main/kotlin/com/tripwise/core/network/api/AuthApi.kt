package com.tripwise.core.network.api

import com.tripwise.core.network.dto.AuthResponse
import com.tripwise.core.network.dto.DeleteAccountRequest
import com.tripwise.core.network.dto.LoginRequest
import com.tripwise.core.network.dto.PatchMeRequest
import com.tripwise.core.network.dto.RefreshRequest
import com.tripwise.core.network.dto.RegisterRequest
import com.tripwise.core.network.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST

/** Unauthenticated endpoints. Served by a client WITHOUT the token interceptor/authenticator. */
interface AuthApi {
    @POST("api/v1/auth/register") suspend fun register(@Body body: RegisterRequest): AuthResponse
    @POST("api/v1/auth/login") suspend fun login(@Body body: LoginRequest): AuthResponse
    @POST("api/v1/auth/refresh") suspend fun refresh(@Body body: RefreshRequest): AuthResponse
    @POST("api/v1/auth/logout") suspend fun logout(@Body body: RefreshRequest)
}

/** Current-user endpoints. Served by the authenticated client. */
interface MeApi {
    @GET("api/v1/me") suspend fun me(): UserDto
    @PATCH("api/v1/me") suspend fun update(@Body body: PatchMeRequest): UserDto
    @HTTP(method = "DELETE", path = "api/v1/me", hasBody = true) suspend fun delete(@Body body: DeleteAccountRequest)
}
