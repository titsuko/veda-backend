package com.titsuko.server.dto.response

data class CardResponse(
    val slug: String,
    val title: String,
    val description: String?,
    val status: String
)
