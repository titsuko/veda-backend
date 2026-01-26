package com.titsuko.server.dto.request

import com.titsuko.server.model.`object`.CardStatus
import jakarta.validation.constraints.NotBlank

data class CardRequest(
    @field:NotBlank(message = "Name is required")
    val title: String? = null,

    val slug: String? = null,
    val description: String? = null,

    @field:NotBlank(message = "Status is required")
    val status: CardStatus? = null
)
