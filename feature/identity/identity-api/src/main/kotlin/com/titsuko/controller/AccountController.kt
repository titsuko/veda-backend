package com.titsuko.controller

import com.titsuko.dto.request.CheckEmailRequest
import com.titsuko.dto.request.RegisterRequest
import com.titsuko.dto.response.AccountResponse
import com.titsuko.dto.response.AuthResponse
import com.titsuko.dto.response.AvailabilityResponse
import com.titsuko.service.AccountService
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
