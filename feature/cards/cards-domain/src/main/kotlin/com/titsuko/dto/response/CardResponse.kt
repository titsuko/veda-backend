package com.titsuko.dto.response

data class CardResponse(
    val id: Long,
    val slug: String,
    val title: String,
    val description: String?,
    val status: String,
    val rarity: String,
    val category: CategoryResponse?
)