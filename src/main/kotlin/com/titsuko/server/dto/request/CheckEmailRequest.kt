package com.titsuko.server.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CheckEmailRequest(
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    val email: String
)