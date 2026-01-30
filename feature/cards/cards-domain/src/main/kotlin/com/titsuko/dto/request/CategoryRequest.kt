package com.titsuko.dto.request

import jakarta.validation.constraints.NotBlank

data class CategoryRequest(
    @field:NotBlank(message = "Title is required")
    val title: String? = null,

    val description: String? = null,
    val slug: String? = null
)