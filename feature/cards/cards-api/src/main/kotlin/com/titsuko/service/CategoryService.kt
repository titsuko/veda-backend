package com.titsuko.service

import com.titsuko.dto.request.CategoryRequest
import com.titsuko.dto.response.CategoryResponse
import com.titsuko.exception.CategoryNotFoundException
import com.titsuko.model.Category
import com.titsuko.repository.CategoryRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository
) {
    private val logger = LoggerFactory.getLogger(CategoryService::class.java)

    @Transactional(readOnly = true)
    fun getAllCategoriesPageable(pageable: Pageable): Page<CategoryResponse> {
        return categoryRepository.findAll(pageable).map { mapToResponse(it) }
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
    fun createCategory(request: CategoryRequest): CategoryResponse {
        val title = requireNotNull(request.title) { "Category title must not be null" }
        val slug = request.slug?.takeIf { it.isNotBlank() }?.let { formatSlug(it) } ?: generateSlug(title)

        val category = categoryRepository.save(
            Category(title = title, slug = slug, description = request.description)
        )
        logger.info("Created category: ${category.title} (ID: ${category.id})")
        return mapToResponse(category)
    }

    @Transactional
    fun updateCategory(id: Long, request: CategoryRequest): CategoryResponse {
        val category = categoryRepository.findByIdOrNull(id)
            ?: throw CategoryNotFoundException()

        category.title = request.title ?: category.title
        category.description = request.description ?: category.description

        if (!request.slug.isNullOrBlank()) {
            category.slug = formatSlug(request.slug!!)
        }

        logger.info("Updated category: ${category.title} (ID: ${category.id})")
        return mapToResponse(categoryRepository.save(category))
    }

    @Transactional
    fun deleteCategory(id: Long) {
        if (!categoryRepository.existsById(id)) {
            throw CategoryNotFoundException()
        }
        categoryRepository.deleteById(id)
        logger.info("Deleted category ID: $id")
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