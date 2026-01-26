package com.titsuko.server.service

import com.titsuko.server.dto.request.CardRequest
import com.titsuko.server.exception.CardNotFoundException
import com.titsuko.server.model.Card
import com.titsuko.server.model.`object`.CardStatus
import com.titsuko.server.repository.CardRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull

@DisplayName("CardService Tests")
class CardServiceTest {

    private val cardRepository: CardRepository = mockk()
    private val cardService = CardService(cardRepository)

    @Nested
    @DisplayName("When creating a card")
    inner class CreateCardTests {

        @Test
        @DisplayName("should create a card with a given slug")
        fun `should create a card with a given slug`() {
            val cardSlot = slot<Card>()
            every { cardRepository.save(capture(cardSlot)) } answers { firstArg() }

            val request = CardRequest(
                title = "New Awesome Card",
                slug = "new-awesome-card",
                description = "This is a test card.",
                status = CardStatus.Public
            )

            val response = cardService.createCard(request)

            assertEquals("New Awesome Card", response.title)
            assertEquals("new-awesome-card", response.slug)
            assertEquals("This is a test card.", response.description)
            assertEquals(CardStatus.Public.toString(), response.status)

            verify(exactly = 1) { cardRepository.save(any()) }
        }

        @Test
        @DisplayName("should generate a slug if it's not provided")
        fun `should generate a slug if it's not provided`() {
            every { cardRepository.existsBySlug("a-generated-slug") } returns false
            every { cardRepository.save(any()) } answers { firstArg() }

            val request = CardRequest(title = "A Generated Slug")

            val response = cardService.createCard(request)

            assertEquals("a-generated-slug", response.slug)
        }

        @Test
        @DisplayName("should generate a unique slug if the generated one already exists")
        fun `should generate a unique slug if the generated one already exists`() {
            every { cardRepository.existsBySlug("existing-slug") } returns true
            every { cardRepository.save(any()) } answers { firstArg() }

            val request = CardRequest(title = "Existing Slug")

            val response = cardService.createCard(request)

            assertTrue(response.slug.startsWith("existing-slug-"))
        }
    }

    @Nested
    @DisplayName("When retrieving cards")
    inner class GetCardTests {

        @Test
        @DisplayName("should return a list of all cards")
        fun `should return a list of all cards`() {
            val cards = listOf(
                Card(id = 1, title = "Card 1", slug = "card-1", description = "Desc 1", status = CardStatus.Public),
                Card(id = 2, title = "Card 2", slug = "card-2", description = "Desc 2", status = CardStatus.Hidden)
            )
            every { cardRepository.findAll() } returns cards

            val response = cardService.getAllCards(null)

            assertEquals(2, response.size)
            assertEquals("Card 1", response[0].title)
        }

        @Test
        @DisplayName("should return a limited list of cards")
        fun `should return a limited list of cards`() {
            val cards = (1..20).map {
                Card(id = it.toLong(), title = "Card $it", slug = "card-$it", description = "Desc $it", status = CardStatus.Public)
            }
            every { cardRepository.findAll() } returns cards

            val response = cardService.getAllCards(5)

            assertEquals(5, response.size)
        }

        @Test
        @DisplayName("should return a card by its ID")
        fun `should return a card by its ID`() {
            val card = Card(id = 1, title = "Found by ID", slug = "found-by-id", description = "", status = CardStatus.Public)
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

        @Test
        @DisplayName("should return a card by its slug")
        fun `should return a card by its slug`() {
            val card = Card(id = 1, title = "Found by Slug", slug = "found-by-slug", description = "", status = CardStatus.Public)
            every { cardRepository.findBySlug("found-by-slug") } returns card

            val response = cardService.getCardBySlug("found-by-slug")

            assertEquals("Found by Slug", response.title)
        }

        @Test
        @DisplayName("should throw CardNotFoundException when card slug does not exist")
        fun `should throw CardNotFoundException when card slug does not exist`() {
            every { cardRepository.findBySlug(any()) } returns null

            assertThrows(CardNotFoundException::class.java) {
                cardService.getCardBySlug("non-existent-slug")
            }
        }
    }
}
