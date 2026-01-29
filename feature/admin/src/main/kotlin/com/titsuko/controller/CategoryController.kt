package com.titsuko.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/category")
class CategoryController {

    @GetMapping
    fun showPage(): String = "category"
}
