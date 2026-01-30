package com.titsuko.controller

import com.titsuko.dto.request.CardRequest
import com.titsuko.service.CardService
import com.titsuko.service.CategoryService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin/cards")
class AdminCardController(
    private val cardService: CardService,
    private val categoryService: CategoryService
) {

    @GetMapping
    fun getPage(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") limit: Int,
        model: Model
    ): String {
        val pageable = PageRequest.of(page, limit, Sort.by("id").descending())

        val cardsPage = cardService.getAllCardsPageable(pageable)
        val categories = categoryService.getAllCategories()

        model.addAttribute("cardsPage", cardsPage)
        model.addAttribute("categories", categories)
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", cardsPage.totalPages)

        return "cards"
    }

    @PostMapping("/create")
    fun createCard(@ModelAttribute request: CardRequest): String {
        try {
            cardService.createCard(request)
        } catch (e: Exception) {
            return "redirect:/admin/cards?error=${e.message}"
        }
        return "redirect:/admin/cards"
    }

    @PostMapping("/{id}/update")
    fun updateCard(
        @PathVariable id: Long,
        @ModelAttribute request: CardRequest
    ): String {
        try {
            cardService.updateCard(id, request)
        } catch (e: Exception) {
            return "redirect:/admin/cards?error=${e.message}"
        }
        return "redirect:/admin/cards"
    }

    @PostMapping("/{id}/delete")
    fun deleteCard(@PathVariable id: Long): String {
        try {
            cardService.deleteCard(id)
        } catch (e: Exception) {
            return "redirect:/admin/cards?error=${e.message}"
        }
        return "redirect:/admin/cards"
    }
}