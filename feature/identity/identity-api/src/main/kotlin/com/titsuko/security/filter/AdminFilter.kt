package com.titsuko.security.filter

import com.titsuko.model.AdminOnly
import com.titsuko.model.ErrorResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerMapping
import tools.jackson.databind.ObjectMapper

@Component
class AdminFilter(
    private val objectMapper: ObjectMapper,

    @param:Qualifier("requestMappingHandlerMapping")
    private val handlerMapping: HandlerMapping
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val handlerExecutionChain = handlerMapping.getHandler(request)
        val handler = handlerExecutionChain?.handler

        if (handler is HandlerMethod) {
            val hasAnnotation = handler.hasMethodAnnotation(AdminOnly::class.java) ||
                    handler.beanType.isAnnotationPresent(AdminOnly::class.java)

            if (hasAnnotation) {
                val auth = SecurityContextHolder.getContext().authentication
                val roles = auth?.authorities?.map { it.authority } ?: emptyList()

                if (!roles.contains("ROLE_ADMIN")) {
                    sendForbiddenError(response)
                    return
                }
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun sendForbiddenError(response: HttpServletResponse) {
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = "application/json"
        val error = ErrorResponse(
            status = HttpServletResponse.SC_FORBIDDEN,
            message = "Access denied: Admin role required"
        )
        response.writer.write(objectMapper.writeValueAsString(error))
    }
}