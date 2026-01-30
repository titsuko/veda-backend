package com.titsuko.service

import com.titsuko.dto.request.CategoryRequest
import com.titsuko.dto.response.CategoryResponse
import com.titsuko.exception.CategoryNotFoundException
import com.titsuko.model.Category
import com.titsuko.repository.CategoryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository
) {

    @Transactional
    fun createCategory(request: CategoryRequest): CategoryResponse {
        val titleToUse = requireNotNull(request.title) { "Category title must not be null" }
        val slug = if (!request.slug.isNullOrBlank()) {
            formatSlug(request.slug!!)
        } else {
            generateSlug(titleToUse)
        }

        val savedCategory = categoryRepository.save(
            Category(
                title = titleToUse,
                slug = slug,
                description = request.description
            )
        )

        return mapToResponse(savedCategory)
    }

    @Transactional(readOnly = true)
    fun getAllCategories(): List<CategoryResponse> {
        return categoryRepository.findAll().map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getCategoryById(id: Long): CategoryResponse {
        val category = categoryRepository.findByIdOrNull(id)
            ?: throw CategoryNotFoundException()
        return mapToResponse(category)
    }

    @Transactional
    fun updateCategory(id: Long, request: CategoryRequest): CategoryResponse {
        val category = categoryRepository.findByIdOrNull(id)
            ?: throw CategoryNotFoundException()

        category.apply {
            title = request.title ?: this.title
            slug = if (!request.slug.isNullOrBlank()) {
                formatSlug(request.slug!!)
            } else {
                generateSlug(title)
            }
            description = request.description
        }

        return mapToResponse(categoryRepository.save(category))
    }

    @Transactional
    fun deleteCategory(id: Long) {
        if (!categoryRepository.existsById(id)) {
            throw CategoryNotFoundException()
        }
        categoryRepository.deleteById(id)
    }

    private fun generateSlug(input: String): String {
        val baseSlug = formatSlug(input)
        return if (categoryRepository.existsBySlug(baseSlug)) {
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

    private fun mapToResponse(category: Category): CategoryResponse {
        return CategoryResponse(
            id = category.id,
            slug = category.slug,
            title = category.title,
            description = category.description,
            cardsCount = category.cards.size
        )
    }
}