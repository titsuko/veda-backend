package com.titsuko.dto.response

data class UserResponse(
    val id: Long,
    val fullName: String,
    val email: String,
    val role: String,
    val createdAt: String,
    val updatedAt: String
)
