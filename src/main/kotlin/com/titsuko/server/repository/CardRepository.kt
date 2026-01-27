package com.titsuko.server.repository

import com.titsuko.server.model.Card
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CardRepository : JpaRepository<Card, Long> {
    fun findBySlug(slug: String): Card?
    fun existsBySlug(slug: String): Boolean
    fun findByCategoryId(categoryId: Long, pageable: Pageable): List<Card>
    @Query("SELECT c FROM Card c WHERE lower(c.title) LIKE lower(concat('%', :query, '%'))")
    fun searchByTitle(query: String): List<Card>
}
