package com.titsuko.controller

import com.titsuko.dto.request.CategoryRequest
import com.titsuko.service.CategoryService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin/category")
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping
    fun getPage(model: Model): String {
        val categories = categoryService.getAllCategories()
        model.addAttribute("categories", categories)
        return "category"
    }

    @PostMapping("/create")
    fun createCategory(@ModelAttribute request: CategoryRequest): String {
        try {
            categoryService.createCategory(request)
        } catch (e: Exception) {
            return "redirect:/admin/category?error=${e.message}"
        }
        return "redirect:/admin/category"
    }

    @PostMapping("/{id}/update")
    fun updateCategory(@PathVariable id: Long, @ModelAttribute request: CategoryRequest): String {
        try {
            categoryService.updateCategory(id, request)
        } catch (e: Exception) {
            return "redirect:/admin/category?error=${e.message}"
        }
        return "redirect:/admin/category"
    }

    @PostMapping("/{id}/delete")
    fun deleteCategory(@PathVariable id: Long): String {
        try {
            categoryService.deleteCategory(id)
        } catch (e: Exception) {
            return "redirect:/admin/category?error=${e.message}"
        }
        return "redirect:/admin/category"
    }
}