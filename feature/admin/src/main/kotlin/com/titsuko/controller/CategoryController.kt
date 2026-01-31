package com.titsuko.controller

import com.titsuko.dto.request.CategoryRequest
import com.titsuko.service.CategoryService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin/category")
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping
    fun getPage(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "7") limit: Int,
        model: Model
    ): String {
        val pageable = PageRequest.of(page, limit, Sort.by("id").ascending())
        val categoriesPage = categoryService.getAllCategoriesPageable(pageable)

        model.addAttribute("categoriesPage", categoriesPage)
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", categoriesPage.totalPages)

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