package com.titsuko.dto.response

data class CategoryResponse(
    val id: Long,
    val slug: String,
    val title: String,
    val description: String?,
    val cardsCount: Int
)