package com.titsuko.server.dto.response

data class CardCategoryResponse(
    val id: Long,
    val slug: String,
    val title: String,
    val description: String?,
    val cardsCount: Int
)