package com.titsuko.controller

import com.titsuko.dto.request.CategoryRequest
import com.titsuko.dto.response.CategoryResponse
import com.titsuko.service.CategoryService
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
import org.springframework.web.bind.annotation.ResponseStatus

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCategory(@Valid @RequestBody request: CategoryRequest): CategoryResponse {
        return categoryService.createCategory(request)
    }

    @GetMapping
    fun getAllCategories(): List<CategoryResponse> {
        return categoryService.getAllCategories()
    }

    @GetMapping("/{id}")
    fun getCategoryById(@PathVariable id: Long): CategoryResponse {
        return categoryService.getCategoryById(id)
    }

    @PutMapping("/{id}")
    fun updateCategory(@PathVariable id: Long, @Valid @RequestBody request: CategoryRequest): CategoryResponse {
        return categoryService.updateCategory(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCategory(@PathVariable id: Long) {
        categoryService.deleteCategory(id)
    }
}