package com.titsuko.server.service

import com.titsuko.server.dto.request.CardCategoryRequest
import com.titsuko.server.exception.CategoryNotFoundException
import com.titsuko.server.model.CardCategory
import com.titsuko.server.repository.CardCategoryRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull

@DisplayName("CardCategoryService Tests")
class CardCategoryServiceTest {

    private val cardCategoryRepository: CardCategoryRepository = mockk()
    private lateinit var categoryService: CardCategoryService

    @BeforeEach
    fun setUp() {
        categoryService = CardCategoryService(cardCategoryRepository)
    }

    @Nested
    @DisplayName("When creating a category")
    inner class CreateCategoryTests {

        @Test
        @DisplayName("should create a category with a given slug")
        fun `should create a category with a given slug`() {
            val categorySlot = slot<CardCategory>()
            every { cardCategoryRepository.save(capture(categorySlot)) } answers { firstArg() }
            every { cardCategoryRepository.existsBySlug("new-category") } returns false

            val request = CardCategoryRequest(
                title = "New Category",
                slug = "new-category",
                description = "A test category"
            )

            val response = categoryService.createCategory(request)

            assertEquals("New Category", response.title)
            assertEquals("new-category", response.slug)
            assertEquals("A test category", response.description)
            assertEquals(0, response.cardsCount)

            verify(exactly = 1) { cardCategoryRepository.save(any()) }
        }

        @Test
        @DisplayName("should generate a slug if it's not provided")
        fun `should generate a slug if it's not provided`() {
            every { cardCategoryRepository.existsBySlug("a-generated-slug") } returns false
            every { cardCategoryRepository.save(any()) } answers { firstArg() }

            val request = CardCategoryRequest(title = "A Generated Slug")

            val response = categoryService.createCategory(request)

            assertEquals("a-generated-slug", response.slug)
        }
    }

    @Nested
    @DisplayName("When retrieving categories")
    inner class GetCategoryTests {

        @Test
        @DisplayName("should return a list of all categories")
        fun `should return a list of all categories`() {
            val categories = listOf(
                CardCategory(id = 1, title = "Category 1", slug = "category-1", description = "Desc 1"),
                CardCategory(id = 2, title = "Category 2", slug = "category-2", description = "Desc 2")
            )
            every { cardCategoryRepository.findAll() } returns categories

            val response = categoryService.getAllCategories()

            assertEquals(2, response.size)
            assertEquals("Category 1", response[0].title)
        }

        @Test
        @DisplayName("should return a category by its ID")
        fun `should return a category by its ID`() {
            val category = CardCategory(id = 1, title = "Found by ID", slug = "found-by-id", description = "")
            every { cardCategoryRepository.findByIdOrNull(1L) } returns category

            val response = categoryService.getCategoryById(1L)

            assertEquals("Found by ID", response.title)
        }

        @Test
        @DisplayName("should throw CategoryNotFoundException when category ID does not exist")
        fun `should throw CategoryNotFoundException when category ID does not exist`() {
            every { cardCategoryRepository.findByIdOrNull(any()) } returns null

            assertThrows(CategoryNotFoundException::class.java) {
                categoryService.getCategoryById(999L)
            }
        }
    }

    @Nested
    @DisplayName("When updating a category")
    inner class UpdateCategoryTests {

        @Test
        @DisplayName("should update an existing category")
        fun `should update an existing category`() {
            val existingCategory = CardCategory(id = 1, title = "Old Title", slug = "old-slug", description = "Old desc")
            every { cardCategoryRepository.findByIdOrNull(1L) } returns existingCategory
            every { cardCategoryRepository.save(any()) } answers { firstArg() }
            every { cardCategoryRepository.existsBySlug("new-slug") } returns false

            val request = CardCategoryRequest(
                title = "New Title",
                slug = "new-slug",
                description = "New description"
            )

            val response = categoryService.updateCategory(1L, request)

            assertEquals("New Title", response.title)
            assertEquals("new-slug", response.slug)
            assertEquals("New description", response.description)
        }

        @Test
        @DisplayName("should throw CategoryNotFoundException when updating a non-existent category")
        fun `should throw CategoryNotFoundException when updating a non-existent category`() {
            every { cardCategoryRepository.findByIdOrNull(any()) } returns null

            val request = CardCategoryRequest(title = "Doesn't matter")

            assertThrows(CategoryNotFoundException::class.java) {
                categoryService.updateCategory(999L, request)
            }
        }
    }

    @Nested
    @DisplayName("When deleting a category")
    inner class DeleteCategoryTests {

        @Test
        @DisplayName("should delete an existing category")
        fun `should delete an existing category`() {
            every { cardCategoryRepository.existsById(1L) } returns true
            every { cardCategoryRepository.deleteById(1L) } returns Unit

            categoryService.deleteCategory(1L)

            verify(exactly = 1) { cardCategoryRepository.deleteById(1L) }
        }

        @Test
        @DisplayName("should throw CategoryNotFoundException when deleting a non-existent category")
        fun `should throw CategoryNotFoundException when deleting a non-existent category`() {
            every { cardCategoryRepository.existsById(any()) } returns false

            assertThrows(CategoryNotFoundException::class.java) {
                categoryService.deleteCategory(999L)
            }
        }
    }
}
