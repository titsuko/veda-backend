package com.titsuko.server.repository

import com.titsuko.server.model.Card
import org.springframework.data.jpa.repository.JpaRepository

interface CardRepository : JpaRepository<Card, Long> {
    fun findBySlug(slug: String): Card?
    fun existsBySlug(slug: String): Boolean
}
