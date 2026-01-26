package com.titsuko.server.service

import com.titsuko.server.dto.request.CardRequest
import com.titsuko.server.dto.response.CardResponse
import com.titsuko.server.exception.CardNotFoundException
import com.titsuko.server.model.Card
import com.titsuko.server.model.`object`.CardStatus
import com.titsuko.server.repository.CardRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CardService(
    private val cardRepository: CardRepository
) {

    @Transactional
    fun createCard(request: CardRequest): CardResponse {
        val slug = if (!request.slug.isNullOrBlank()) {
            formatSlug(request.slug)
        } else {
            generateSlug(request.title ?: "card")
        }

        val savedCard = cardRepository.save(Card(
            title = requireNotNull(request.title),
            slug = slug,
            description = request.description,
            status = request.status ?: CardStatus.Hidden
        ))

        return mapToResponse(savedCard)
    }

    @Transactional(readOnly = true)
    fun getAllCards(limit: Int?): List<CardResponse> {
        return cardRepository.findAll()
            .take(limit ?: 10)
            .map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getCardById(id: Long): CardResponse {
        val card = cardRepository.findByIdOrNull(id)
            ?: throw CardNotFoundException()
        return mapToResponse(card)
    }

    fun getCardBySlug(slug: String): CardResponse {
        val card = cardRepository.findBySlug(slug)
            ?: throw CardNotFoundException()
        return mapToResponse(card)
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
            slug = card.slug,
            title = card.title,
            description = card.description,
            status = card.status.toString()
        )
    }
}
