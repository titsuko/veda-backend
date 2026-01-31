package com.titsuko.service

import com.titsuko.exception.CardNotFoundException
import com.titsuko.model.ContentBlock
import com.titsuko.repository.CardRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@Service
class ContentService(
    private val cardRepository: CardRepository,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(ContentService::class.java)

    @Transactional(readOnly = true)
    fun getContentAsJson(cardId: Long): String {
        val card = cardRepository.findByIdOrNull(cardId)
            ?: throw CardNotFoundException()

        return if (card.content.isNotEmpty()) {
            try {
                objectMapper.writeValueAsString(card.content)
            } catch (e: Exception) {
                logger.error("Failed to serialize content for card $cardId", e)
                "[]"
            }
        } else {
            "[]"
        }
    }

    @Transactional
    fun updateContent(cardId: Long, newBlocks: List<ContentBlock>) {
        val card = cardRepository.findByIdOrNull(cardId)
            ?: throw CardNotFoundException()

        card.content = newBlocks
        cardRepository.save(card)

        logger.debug("Updated content for card $cardId (${newBlocks.size} blocks)")
    }
}