package com.titsuko.repository

import com.titsuko.model.Category
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long> {
    fun existsBySlug(slug: String): Boolean
}