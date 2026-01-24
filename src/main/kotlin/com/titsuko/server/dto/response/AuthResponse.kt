package com.titsuko.server.dto.response

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)
