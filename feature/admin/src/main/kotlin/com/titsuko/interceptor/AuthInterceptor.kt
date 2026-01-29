package com.titsuko.interceptor

import com.titsuko.model.`object`.Role
import com.titsuko.security.JwtService
import com.titsuko.service.AccountService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor(
    private val jwtService: JwtService,
    private val accountService: AccountService
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val token = request.cookies
            ?.firstOrNull { it.name == "access_token" }
            ?.value

        if (token.isNullOrBlank() || !jwtService.isTokenValid(token)) {
            response.sendRedirect("/admin/login")
            return false
        }

        val email = jwtService.extractEmail(token)
            ?: response.sendRedirect("/admin/login")

        val auth = UsernamePasswordAuthenticationToken(email, null, emptyList())
        SecurityContextHolder.getContext().authentication = auth

        try {
            val profile = accountService.getProfile()
            println(profile.role)

            if (profile.role != Role.ADMIN.toString()) {
                response.sendRedirect("/admin/login")
                return false
            }

            return true
        }
        catch (_: Exception) {
            response.sendRedirect("/admin/login")
            return false
        }

    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        SecurityContextHolder.clearContext()
    }
}