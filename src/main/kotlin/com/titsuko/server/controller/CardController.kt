package com.titsuko.server.controller

import com.titsuko.server.dto.request.CardRequest
import com.titsuko.server.dto.response.CardResponse
import com.titsuko.server.service.CardService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus

@RestController
@RequestMapping("/api/cards")
class CardController(
    private val cardService: CardService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCard(@Valid @RequestBody request: CardRequest): CardResponse {
        return cardService.createCard(request)
    }

    @GetMapping
    fun getCards(
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false) categoryId: Long?
    ): List<CardResponse> {
        return cardService.getAllCards(limit ?: 10, categoryId)
    }

    @GetMapping("/{id}")
    fun getCardById(@PathVariable id: Long): CardResponse {
        return cardService.getCardById(id)
    }

    @GetMapping("/slug/{slug}")
    fun getCardBySlug(@PathVariable slug: String): CardResponse {
        return cardService.getCardBySlug(slug)
    }

    @GetMapping("/search")
    fun searchCards(@RequestParam query: String): List<CardResponse> {
        return cardService.searchCards(query)
    }

    @PutMapping("/{id}")
    fun updateCard(
        @PathVariable id: Long,
        @Valid @RequestBody request: CardRequest
    ): CardResponse {
        return cardService.updateCard(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCard(@PathVariable id: Long) {
        cardService.deleteCard(id)
    }
}
