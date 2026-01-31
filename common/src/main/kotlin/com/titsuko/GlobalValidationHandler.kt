package com.titsuko

import com.titsuko.exception.AppException
import com.titsuko.model.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(AppException::class)
    fun handleAppException(ex: AppException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.warn("AppException: {} | Status: {} | URI: {}", ex.message, ex.status, request.requestURI)

        val errorResponse = ErrorResponse(
            status = ex.status.value(),
            message = ex.message
        )

        return ResponseEntity(errorResponse, ex.status)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errorMessage = ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        logger.warn("Validation Error: {} | URI: {}", errorMessage, request.requestURI)

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = errorMessage
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected Error: {} | URI: {}", ex.message, request.requestURI, ex)

        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "An unexpected internal server error occurred"
        )

        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}