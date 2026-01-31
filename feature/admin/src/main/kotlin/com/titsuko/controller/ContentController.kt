package com.titsuko.controller

import com.titsuko.model.ContentBlock
import com.titsuko.service.CardService
import com.titsuko.service.ContentService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.ObjectMapper

@Controller
@RequestMapping("/admin/cards/content")
class ContentController(
    private val cardService: CardService,
    private val contentService: ContentService,
    private val objectMapper: ObjectMapper
) {

    @GetMapping("/{id}")
    fun openEditor(@PathVariable id: Long, model: Model): String {
        val card = cardService.getCardById(id)
        val content = card.content ?: emptyList()
        val jsonContent = objectMapper.writeValueAsString(content)

        model.addAttribute("card", card)
        model.addAttribute("contentJson", if (jsonContent == "null" || jsonContent.isEmpty()) "[]" else jsonContent)

        return "content-editor"
    }

    @PutMapping("/{id}/save")
    @ResponseBody
    fun saveContent(
        @PathVariable id: Long,
        @RequestBody blocks: List<ContentBlock>
    ): Map<String, Any> {
        return try {
            contentService.updateContent(id, blocks)
            mapOf("status" to "success")
        } catch (e: Exception) {
            mapOf("status" to "error", "message" to (e.message ?: "Unknown server error"))
        }
    }
}