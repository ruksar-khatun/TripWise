package com.tripwise.core.network.datasource

import com.tripwise.core.common.result.Outcome
import com.tripwise.core.network.api.AuthApi
import com.tripwise.core.network.api.MeApi
import com.tripwise.core.network.dto.AuthResponse
import com.tripwise.core.network.dto.DeleteAccountRequest
import com.tripwise.core.network.dto.LoginRequest
import com.tripwise.core.network.dto.RefreshRequest
import com.tripwise.core.network.dto.RegisterRequest
import com.tripwise.core.network.dto.UserDto
import com.tripwise.core.network.error.ApiErrorMapper
import com.tripwise.core.network.error.call
import javax.inject.Inject
import javax.inject.Singleton

interface AuthNetworkDataSource {
    suspend fun register(email: String, password: String, displayName: String): Outcome<AuthResponse>
    suspend fun login(email: String, password: String): Outcome<AuthResponse>
    suspend fun logout(refreshToken: String): Outcome<Unit>
    suspend fun me(): Outcome<UserDto>
    suspend fun deleteAccount(password: String): Outcome<Unit>
}

@Singleton
internal class RetrofitAuthNetworkDataSource @Inject constructor(
    private val authApi: AuthApi,
    private val meApi: MeApi,
    private val errors: ApiErrorMapper,
) : AuthNetworkDataSource {
    override suspend fun register(email: String, password: String, displayName: String) =
        errors.call { authApi.register(RegisterRequest(email, password, displayName)) }

    override suspend fun login(email: String, password: String) = errors.call { authApi.login(LoginRequest(email, password)) }
    override suspend fun logout(refreshToken: String) = errors.call { authApi.logout(RefreshRequest(refreshToken)) }
    override suspend fun me() = errors.call { meApi.me() }
    override suspend fun deleteAccount(password: String) = errors.call { meApi.delete(DeleteAccountRequest(password)) }
}
