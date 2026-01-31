package com.titsuko.config

import com.titsuko.service.AccountService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

@Component
class AuthInterceptor(
    private val accountService: AccountService
) : HandlerInterceptor {

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        if (modelAndView != null && !modelAndView.viewName?.startsWith("redirect:")!!) {
            try {
                val profile = accountService.getProfile()
                modelAndView.addObject("username", "${profile.firstName} ${profile.lastName}")
                modelAndView.addObject("role", profile.role)
            } catch (_: Exception) {
                // Ignore if user profile cannot be loaded for UI
            }
        }
    }
}