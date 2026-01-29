package com.titsuko.controller

import com.titsuko.dto.request.LoginRequest
import com.titsuko.exception.InvalidCredentialsException
import com.titsuko.exception.UserNotFoundException
import com.titsuko.service.SessionService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin")
class AuthController(
    private val sessionService: SessionService
) {

    @PostMapping("/login")
    fun handleLogin(
        @ModelAttribute request: LoginRequest,
        response: HttpServletResponse,
        model: Model
    ): String {
        return try {
            val model = sessionService.login(request)
            response.addCookie(Cookie("access_token", model.accessToken).apply {
                isHttpOnly = true
                path = "/"
                maxAge = model.expiresIn.toInt()
            })

            "redirect:/admin"
        } catch (e: UserNotFoundException) {
            model.addAttribute("error", e.message)
            "login"
        } catch (e: InvalidCredentialsException) {
            model.addAttribute("error", e.message)
            "login"
        }
    }

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): String {
        response.addCookie(Cookie("access_token", null).apply {
            path = "/"
            maxAge = 0
        })
        return "redirect:/admin/login"
    }

    @GetMapping("/login")
    fun loginPage(): String = "login"
}
