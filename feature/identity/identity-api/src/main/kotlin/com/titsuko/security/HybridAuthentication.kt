package com.titsuko.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class HybridAuthentication : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val uri = request.requestURI
        val acceptHeader = request.getHeader("Accept") ?: ""

        val isApiRequest = acceptHeader.contains("application/json") ||
                uri.startsWith("/api") ||
                uri.contains("/save")

        if (isApiRequest) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json"
            response.characterEncoding = "UTF-8"
            response.writer.write("""{"status": 401, "error": "Unauthorized", "message": "Session expired"}""")
        } else {
            response.sendRedirect("/admin/login")
        }
    }
}