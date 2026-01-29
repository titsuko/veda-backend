package com.titsuko.controller

import com.titsuko.service.AccountService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin")
class HomeController(
    private val accountService: AccountService
) {

    @GetMapping
    fun getPage(model: Model): String {
        val profile = accountService.getProfile()
        model.addAttribute("username", "${profile.lastName} ${profile.firstName}")
        model.addAttribute("role", profile.role)

        return "home"

    }
}