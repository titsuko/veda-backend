package com.titsuko.server.service

import com.titsuko.server.dto.request.CardRequest
import com.titsuko.server.exception.CardNotFoundException
import com.titsuko.server.model.Card
import com.titsuko.server.model.CardCategory
import com.titsuko.server.model.`object`.CardRarity
import com.titsuko.server.model.`object`.CardStatus
import com.titsuko.server.repository.CardCategoryRepository
import com.titsuko.server.repository.CardRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull

@DisplayName("CardService Tests")
class CardServiceTest {

    private val cardRepository: CardRepository = mockk()
    private val cardCategoryRepository: CardCategoryRepository = mockk()
    private lateinit var cardService: CardService

    @BeforeEach
    fun setUp() {
        cardService = CardService(cardRepository, cardCategoryRepository)
    }

    @Nested
    @DisplayName("When creating a card")
    inner class CreateCardTests {

        @Test
        @DisplayName("should create a card with a given slug")
        fun `should create a card with a given slug`() {
            val cardSlot = slot<Card>()
            every { cardRepository.save(capture(cardSlot)) } answers { firstArg() }
            every { cardCategoryRepository.findByIdOrNull(any()) } returns null

            val request = CardRequest(
                title = "New Awesome Card",
                slug = "new-awesome-card",
                description = "This is a test card.",
                status = CardStatus.Public,
                rarity = CardRarity.RARE,
                categoryId = null
            )

            val response = cardService.createCard(request)

            assertEquals("New Awesome Card", response.title)
            assertEquals("new-awesome-card", response.slug)
            assertEquals("This is a test card.", response.description)
            assertEquals(CardStatus.Public.toString(), response.status)
            assertEquals(CardRarity.RARE.toString(), response.rarity)
            assertNull(response.category)

            verify(exactly = 1) { cardRepository.save(any()) }
        }

        @Test
        @DisplayName("should create a card with a category")
        fun `should create a card with a category`() {
            val category = CardCategory(id = 1, title = "Category", slug = "category", description = "")
            every { cardRepository.save(any()) } answers { firstArg() }
            every { cardCategoryRepository.findByIdOrNull(1L) } returns category
            every { cardRepository.existsBySlug(any()) } returns false

            val request = CardRequest(
                title = "Card with Category",
                categoryId = 1L
            )

            val response = cardService.createCard(request)

            assertNotNull(response.category)
            assertEquals("Category", response.category?.title)
        }
    }

    @Nested
    @DisplayName("When retrieving cards")
    inner class GetCardTests {

        @Test
        @DisplayName("should return a list of all cards")
        fun `should return a list of all cards`() {
            val cards = listOf(
                Card(id = 1, title = "Card 1", slug = "card-1", description = "Desc 1", status = CardStatus.Public, rarity = CardRarity.COMMON),
                Card(id = 2, title = "Card 2", slug = "card-2", description = "Desc 2", status = CardStatus.Hidden, rarity = CardRarity.COMMON)
            )
            every { cardRepository.findAll(any<PageRequest>()) } returns PageImpl(cards)

            val response = cardService.getAllCards(10, null)

            assertEquals(2, response.size)
            assertEquals("Card 1", response[0].title)
        }

        @Test
        @DisplayName("should return a limited list of cards")
        fun `should return a limited list of cards`() {
            val cards = (1..20).map {
                Card(id = it.toLong(), title = "Card $it", slug = "card-$it", description = "Desc $it", status = CardStatus.Public, rarity = CardRarity.COMMON)
            }
            every { cardRepository.findAll(PageRequest.of(0, 5)) } returns PageImpl(cards.take(5))

            val response = cardService.getAllCards(5, null)

            assertEquals(5, response.size)
        }

        @Test
        @DisplayName("should return cards filtered by category")
        fun `should return cards filtered by category`() {
            val category = CardCategory(id = 1, title = "Category", slug = "category", description = "")
            val cards = listOf(
                Card(id = 1, title = "Card 1", slug = "card-1", category = category, status = CardStatus.Public, rarity = CardRarity.COMMON),
                Card(id = 3, title = "Card 3", slug = "card-3", category = category, status = CardStatus.Public, rarity = CardRarity.COMMON)
            )
            every { cardRepository.findByCategoryId(1L, any()) } returns cards

            val response = cardService.getAllCards(10, 1L)

            assertEquals(2, response.size)
            assertTrue(response.all { it.category?.id == 1L })
        }

        @Test
        @DisplayName("should return a card by its ID")
        fun `should return a card by its ID`() {
            val card = Card(id = 1, title = "Found by ID", slug = "found-by-id", description = "", status = CardStatus.Public, rarity = CardRarity.COMMON)
            every { cardRepository.findByIdOrNull(1L) } returns card

            val response = cardService.getCardById(1L)

            assertEquals("Found by ID", response.title)
        }

        @Test
        @DisplayName("should throw CardNotFoundException when card ID does not exist")
        fun `should throw CardNotFoundException when card ID does not exist`() {
            every { cardRepository.findByIdOrNull(any()) } returns null

            assertThrows(CardNotFoundException::class.java) {
                cardService.getCardById(999L)
            }
        }
    }

    @Nested
    @DisplayName("When updating a card")
    inner class UpdateCardTests {

        @Test
        @DisplayName("should update an existing card")
        fun `should update an existing card`() {
            val existingCard = Card(id = 1, title = "Old Title", slug = "old-slug", status = CardStatus.Hidden, rarity = CardRarity.COMMON)
            val category = CardCategory(id = 2, title = "New Category", slug = "new-category", description = "")

            every { cardRepository.findByIdOrNull(1L) } returns existingCard
            every { cardRepository.save(any()) } answers { firstArg() }
            every { cardCategoryRepository.findByIdOrNull(2L) } returns category

            val request = CardRequest(
                title = "New Title",
                slug = "new-slug",
                description = "New description",
                status = CardStatus.Public,
                rarity = CardRarity.EPIC,
                categoryId = 2L
            )

            val response = cardService.updateCard(1L, request)

            assertEquals("New Title", response.title)
            assertEquals("new-slug", response.slug)
            assertEquals("New description", response.description)
            assertEquals(CardStatus.Public.toString(), response.status)
            assertEquals(CardRarity.EPIC.toString(), response.rarity)
            assertEquals(2L, response.category?.id)
        }

        @Test
        @DisplayName("should throw CardNotFoundException when updating a non-existent card")
        fun `should throw CardNotFoundException when updating a non-existent card`() {
            every { cardRepository.findByIdOrNull(any()) } returns null

            val request = CardRequest(title = "Doesn't matter")

            assertThrows(CardNotFoundException::class.java) {
                cardService.updateCard(999L, request)
            }
        }
    }

    @Nested
    @DisplayName("When deleting a card")
    inner class DeleteCardTests {

        @Test
        @DisplayName("should delete an existing card")
        fun `should delete an existing card`() {
            every { cardRepository.existsById(1L) } returns true
            every { cardRepository.deleteById(1L) } returns Unit

            cardService.deleteCard(1L)

            verify(exactly = 1) { cardRepository.deleteById(1L) }
        }

        @Test
        @DisplayName("should throw CardNotFoundException when deleting a non-existent card")
        fun `should throw CardNotFoundException when deleting a non-existent card`() {
            every { cardRepository.existsById(any()) } returns false

            assertThrows(CardNotFoundException::class.java) {
                cardService.deleteCard(999L)
            }
        }
    }

    @Nested
    @DisplayName("When searching for cards")
    inner class SearchCardTests {

        @Test
        @DisplayName("should return cards matching the search query")
        fun `should return cards matching the search query`() {
            val cards = listOf(
                Card(id = 1, title = "Awesome Card", slug = "awesome-card", status = CardStatus.Public, rarity = CardRarity.COMMON),
                Card(id = 2, title = "Another Awesome Card", slug = "another-awesome-card", status = CardStatus.Public, rarity = CardRarity.COMMON)
            )
            every { cardRepository.searchByTitle("awesome") } returns cards

            val response = cardService.searchCards("awesome")

            assertEquals(2, response.size)
            assertTrue(response.all { it.title.contains("Awesome", ignoreCase = true) })
        }
    }
}
