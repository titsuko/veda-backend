package com.titsuko.service

import com.titsuko.dto.request.CardRequest
import com.titsuko.dto.response.CategoryResponse
import com.titsuko.dto.response.CardResponse
import com.titsuko.exception.CardNotFoundException
import com.titsuko.model.Card
import com.titsuko.model.`object`.CardRarity
import com.titsuko.model.`object`.CardStatus
import com.titsuko.repository.CategoryRepository
import com.titsuko.repository.CardRepository
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

    @Transactional
    fun createCard(request: CardRequest): CardResponse {
        val titleToUse = requireNotNull(request.title) { "Card title must not be null" }
        val slug = if (!request.slug.isNullOrBlank()) {
            formatSlug(request.slug!!)
        } else {
            generateSlug(titleToUse)
        }

        val category = request.categoryId?.let {
            categoryRepository.findByIdOrNull(it)
        }

        val savedCard = cardRepository.save(
            Card(
                title = titleToUse,
                slug = slug,
                description = request.description,
                status = request.status ?: CardStatus.Hidden,
                rarity = request.rarity ?: CardRarity.COMMON,
                category = category
            )
        )

        return mapToResponse(savedCard)
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
        val card = cardRepository.findByIdOrNull(id)
            ?: throw CardNotFoundException()
        return mapToResponse(card)
    }

    @Transactional(readOnly = true)
    fun getCardBySlug(slug: String): CardResponse {
        val card = cardRepository.findBySlug(slug)
            ?: throw CardNotFoundException()
        return mapToResponse(card)
    }

    @Transactional(readOnly = true)
    fun searchCards(query: String): List<CardResponse> {
        return cardRepository.searchByTitle(query).map { mapToResponse(it) }
    }

    @Transactional
    fun updateCard(id: Long, request: CardRequest): CardResponse {
        val card = cardRepository.findByIdOrNull(id)
            ?: throw CardNotFoundException()

        val category = request.categoryId?.let {
            categoryRepository.findByIdOrNull(it)
        }

        card.apply {
            val newTitle = request.title ?: this.title
            title = newTitle
            slug = if (!request.slug.isNullOrBlank()) {
                formatSlug(request.slug!!)
            } else {
                generateSlug(newTitle)
            }
            description = request.description
            status = request.status ?: CardStatus.Hidden
            rarity = request.rarity ?: CardRarity.COMMON
            this.category = category
        }

        return mapToResponse(cardRepository.save(card))
    }

    @Transactional
    fun deleteCard(id: Long) {
        if (!cardRepository.existsById(id)) {
            throw CardNotFoundException()
        }
        cardRepository.deleteById(id)
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