package com.titsuko.model.content

import com.titsuko.model.ContentBlock

data class HeaderBlock(
    override val type: String = "header", // Явно задаем тип
    val text: String,
    val level: Int? = 2
) : ContentBlock