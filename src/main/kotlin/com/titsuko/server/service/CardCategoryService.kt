package com.titsuko.server.service

import com.titsuko.server.dto.request.CardCategoryRequest
import com.titsuko.server.dto.response.CardCategoryResponse
import com.titsuko.server.exception.CategoryNotFoundException
import com.titsuko.server.model.CardCategory
import com.titsuko.server.repository.CardCategoryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CardCategoryService(
    private val cardCategoryRepository: CardCategoryRepository
) {

    @Transactional
    fun createCategory(request: CardCategoryRequest): CardCategoryResponse {
        val titleToUse = requireNotNull(request.title) { "Category title must not be null" }
        val slug = if (!request.slug.isNullOrBlank()) {
            formatSlug(request.slug)
        } else {
            generateSlug(titleToUse)
        }

        val savedCategory = cardCategoryRepository.save(
            CardCategory(
                title = titleToUse,
                slug = slug,
                description = request.description
            )
        )

        return mapToResponse(savedCategory)
    }

    @Transactional(readOnly = true)
    fun getAllCategories(): List<CardCategoryResponse> {
        return cardCategoryRepository.findAll().map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getCategoryById(id: Long): CardCategoryResponse {
        val category = cardCategoryRepository.findByIdOrNull(id)
            ?: throw CategoryNotFoundException()
        return mapToResponse(category)
    }

    @Transactional
    fun updateCategory(id: Long, request: CardCategoryRequest): CardCategoryResponse {
        val category = cardCategoryRepository.findByIdOrNull(id)
            ?: throw CategoryNotFoundException()

        category.apply {
            title = request.title ?: this.title
            slug = if (!request.slug.isNullOrBlank()) {
                formatSlug(request.slug)
            } else {
                generateSlug(title)
            }
            description = request.description
        }

        return mapToResponse(cardCategoryRepository.save(category))
    }

    @Transactional
    fun deleteCategory(id: Long) {
        if (!cardCategoryRepository.existsById(id)) {
            throw CategoryNotFoundException()
        }
        cardCategoryRepository.deleteById(id)
    }

    private fun generateSlug(input: String): String {
        val baseSlug = formatSlug(input)
        return if (cardCategoryRepository.existsBySlug(baseSlug)) {
            "$baseSlug-${UUID.randomUUID().toString().take(5)}"
        } else {
            baseSlug
        }
    }

    private fun formatSlug(input: String): String {
        return input.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')
    }

    private fun mapToResponse(category: CardCategory): CardCategoryResponse {
        return CardCategoryResponse(
            id = category.id,
            slug = category.slug,
            title = category.title,
            description = category.description,
            cardsCount = category.cards.size
        )
    }
}
