package com.tripwise.core.network.dto

import kotlinx.serialization.Serializable

@Serializable data class RegisterRequest(val email: String, val password: String, val displayName: String)
@Serializable data class LoginRequest(val email: String, val password: String)
@Serializable data class RefreshRequest(val refreshToken: String)
@Serializable data class DeleteAccountRequest(val password: String)
@Serializable data class PatchMeRequest(val displayName: String? = null, val homeCurrency: String? = null)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val homeCurrency: String,
    val createdAt: String,
)

@Serializable
data class TokensDto(
    val accessToken: String,
    val accessTokenExpiresIn: Long,
    val refreshToken: String,
    val refreshTokenExpiresAt: String,
)

@Serializable data class AuthResponse(val user: UserDto, val tokens: TokensDto)
