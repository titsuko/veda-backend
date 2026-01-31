package com.titsuko.service

import com.titsuko.dto.request.CardRequest
import com.titsuko.dto.response.CardResponse
import com.titsuko.dto.response.CategoryResponse
import com.titsuko.exception.CardNotFoundException
import com.titsuko.model.Card
import com.titsuko.model.`object`.CardRarity
import com.titsuko.model.`object`.CardStatus
import com.titsuko.repository.CardRepository
import com.titsuko.repository.CategoryRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CardService(
    private val cardRepository: CardRepository,
    private val categoryRepository: CategoryRepository
) {
    private val logger = LoggerFactory.getLogger(CardService::class.java)

    @Transactional
    fun createCard(request: CardRequest): CardResponse {
        val title = requireNotNull(request.title) { "Card title must not be null" }
        val slug = request.slug?.takeIf { it.isNotBlank() }?.let { formatSlug(it) } ?: generateSlug(title)

        val category = request.categoryId?.let { categoryRepository.findByIdOrNull(it) }

        val card = cardRepository.save(
            Card(
                title = title,
                slug = slug,
                description = request.description,
                content = request.content ?: emptyList(),
                status = request.status ?: CardStatus.Hidden,
                rarity = request.rarity ?: CardRarity.COMMON,
                category = category
            )
        )
        logger.info("Created card: ${card.title} (ID: ${card.id})")
        return mapToResponse(card)
    }

    @Transactional(readOnly = true)
    fun getAllCards(limit: Int, categoryId: Long?): List<CardResponse> {
        val pageable = PageRequest.of(0, limit)
        val cards = if (categoryId != null) {
            cardRepository.findByCategoryId(categoryId, pageable)
        } else {
            cardRepository.findAll(pageable).content
        }
        return cards.map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getAllCardsPageable(pageable: Pageable): Page<CardResponse> {
        return cardRepository.findAll(pageable).map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getCardById(id: Long): CardResponse {
        val card = cardRepository.findByIdOrNull(id) ?: throw CardNotFoundException()
        return mapToResponse(card)
    }

    @Transactional(readOnly = true)
    fun getCardBySlug(slug: String): CardResponse {
        val card = cardRepository.findBySlug(slug) ?: throw CardNotFoundException()
        return mapToResponse(card)
    }

    @Transactional(readOnly = true)
    fun searchCards(query: String): List<CardResponse> {
        return cardRepository.searchByTitle(query).map { mapToResponse(it) }
    }

    @Transactional
    fun updateCard(id: Long, request: CardRequest): CardResponse {
        val card = cardRepository.findByIdOrNull(id) ?: throw CardNotFoundException()

        request.title?.let {
            card.title = it
            if (request.slug.isNullOrBlank()) {
                card.slug = generateSlug(it)
            }
        }

        request.slug?.takeIf { it.isNotBlank() }?.let { card.slug = formatSlug(it) }

        card.description = request.description ?: card.description
        card.content = request.content ?: card.content
        card.status = request.status ?: card.status
        card.rarity = request.rarity ?: card.rarity

        request.categoryId?.let {
            card.category = categoryRepository.findByIdOrNull(it)
        }

        logger.info("Updated card: ${card.title} (ID: ${card.id})")
        return mapToResponse(cardRepository.save(card))
    }

    @Transactional
    fun deleteCard(id: Long) {
        if (!cardRepository.existsById(id)) {
            throw CardNotFoundException()
        }
        cardRepository.deleteById(id)
        logger.info("Deleted card ID: $id")
    }

    private fun generateSlug(input: String): String {
        val baseSlug = formatSlug(input)
        return if (cardRepository.existsBySlug(baseSlug)) {
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

    private fun mapToResponse(card: Card): CardResponse {
        return CardResponse(
            id = card.id,
            slug = card.slug,
            title = card.title,
            description = card.description,
            content = card.content,
            status = card.status.toString(),
            rarity = card.rarity.toString(),
            category = card.category?.let {
                CategoryResponse(
                    id = it.id,
                    slug = it.slug,
                    title = it.title,
                    description = it.description,
                    cardsCount = it.cards.size
                )
            }
        )
    }
}