package com.titsuko.security.filter

import com.titsuko.repository.UserRepository
import com.titsuko.security.JwtService
import com.titsuko.security.RefreshService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val refreshService: RefreshService,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val accessToken = extractToken(request, "access_token")

            if (accessToken != null && jwtService.isTokenValid(accessToken)) {
                authenticateUser(accessToken, request)
            } else {
                val refreshToken = extractToken(request, "refresh_token")
                if (refreshToken != null) {
                    refreshSession(refreshToken, response, request)
                }
            }
        } catch (e: Exception) {
            logger.debug("Authentication failed: {}", e.message)
        }

        filterChain.doFilter(request, response)
    }

    private fun refreshSession(refreshToken: String, response: HttpServletResponse, request: HttpServletRequest) {
        try {
            val (user, newRefreshToken) = refreshService.rotateToken(refreshToken)
            val (newAccessToken, expiry) = jwtService.generateAccessToken(user.email)

            addCookie(response, "access_token", newAccessToken, expiry.toInt())
            addCookie(response, "refresh_token", newRefreshToken, 24 * 60 * 60)

            authenticateUser(newAccessToken, request)
        } catch (e: Exception) {
            logger.warn("Failed to refresh session automatically: {}", e.message)
            addCookie(response, "access_token", "", 0)
            addCookie(response, "refresh_token", "", 0)
        }
    }

    private fun authenticateUser(token: String, request: HttpServletRequest) {
        if (SecurityContextHolder.getContext().authentication == null) {
            val email = jwtService.extractEmail(token) ?: return

            userRepository.findByEmail(email)?.let { user ->
                val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
                val authToken = UsernamePasswordAuthenticationToken(email, null, authorities)
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
            }
        }
    }

    private fun extractToken(request: HttpServletRequest, cookieName: String): String? {
        if (cookieName == "access_token") {
            val header = request.getHeader("Authorization")
            if (header != null && header.startsWith("Bearer ")) {
                return header.substring(7)
            }
        }
        return request.cookies?.firstOrNull { it.name == cookieName }?.value
    }

    private fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Int) {
        val cookie = Cookie(name, value).apply {
            isHttpOnly = true
            path = "/"
            this.maxAge = maxAge
        }
        response.addCookie(cookie)
    }
}