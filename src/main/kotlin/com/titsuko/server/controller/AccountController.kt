package com.titsuko.server.controller

import com.titsuko.server.dto.request.CheckEmailRequest
import com.titsuko.server.dto.request.RegisterRequest
import com.titsuko.server.dto.response.AccountResponse
import com.titsuko.server.dto.response.AuthResponse
import com.titsuko.server.dto.response.AvailabilityResponse
import com.titsuko.server.service.AccountService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/accounts")
class AccountController(
    private val accountService: AccountService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAccount(@Valid @RequestBody request: RegisterRequest): AuthResponse {
        return accountService.register(request)
    }

    @GetMapping("/me")
    fun getProfile(): AccountResponse {
        return accountService.getProfile()
    }

    @PostMapping("/check-email")
    fun checkEmail(@Valid @RequestBody request: CheckEmailRequest): AvailabilityResponse {
        return accountService.checkEmail(request)
    }
}