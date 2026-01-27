package com.titsuko.server.dto.request

import jakarta.validation.constraints.NotBlank

data class CardCategoryRequest(
    @field:NotBlank(message = "Title is required")
    val title: String? = null,

    val description: String? = null,
    val slug: String? = null
)
