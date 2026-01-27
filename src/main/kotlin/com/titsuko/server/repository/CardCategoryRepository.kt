package com.titsuko.server.repository

import com.titsuko.server.model.CardCategory
import org.springframework.data.jpa.repository.JpaRepository

interface CardCategoryRepository : JpaRepository<CardCategory, Long> {
    fun existsBySlug(slug: String): Boolean
}
