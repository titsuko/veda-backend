package com.titsuko.dto.request

data class UpdateUserRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
)
