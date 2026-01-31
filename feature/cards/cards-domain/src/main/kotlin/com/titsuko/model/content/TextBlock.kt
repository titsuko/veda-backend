package com.titsuko.model.content

import com.titsuko.model.ContentBlock

data class TextBlock(
    override val type: String = "text", // Явно задаем тип
    val text: String,
    val isBold: Boolean? = false,
    val isItalic: Boolean? = false,
    val color: String? = null
) : ContentBlock