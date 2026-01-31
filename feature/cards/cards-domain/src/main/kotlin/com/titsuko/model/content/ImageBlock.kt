package com.titsuko.model.content

import com.titsuko.model.ContentBlock

data class ImageBlock(
    override val type: String = "image", // Явно задаем тип
    val url: String,
    val caption: String? = null,
    val ration: Float? = 1.77f
) : ContentBlock
