package com.titsuko.service

import com.titsuko.dto.request.CheckEmailRequest
import com.titsuko.dto.request.RegisterRequest
import com.titsuko.dto.response.AccountResponse
import com.titsuko.dto.response.AuthResponse
import com.titsuko.dto.response.AvailabilityResponse
import com.titsuko.exception.InvalidTokenException
import com.titsuko.exception.UserAlreadyExistsException
import com.titsuko.exception.UserNotFoundException
import com.titsuko.model.Profile
import com.titsuko.model.User
import com.titsuko.repository.ProfileRepository
import com.titsuko.repository.UserRepository
import com.titsuko.security.HashEncoder
import com.titsuko.security.JwtService
import com.titsuko.security.RefreshService
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountService(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val jwtService: JwtService,
    private val refreshService: RefreshService,
    private val hashEncoder: HashEncoder
) {

    private val logger = LoggerFactory.getLogger(AccountService::class.java)

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        val email = requireNotNull(request.email)
        if (userRepository.findByEmail(email) != null) {
            throw UserAlreadyExistsException()
        }

        val hashedPassword = hashEncoder.encode(requireNotNull(request.password)).toString()
        val (firstName, lastName) = parseFullName(requireNotNull(request.fullName))

        val profile = profileRepository.save(
            Profile(firstName = firstName, lastName = lastName)
        )

        val user = userRepository.save(
            User(email = email, password = hashedPassword, profile = profile)
        )

        val accessToken = jwtService.generateAccessToken(user.email)
        val refreshToken = refreshService.createToken(user)

        logger.info("New user registered: $email")

        return AuthResponse(
            accessToken = accessToken.first,
            refreshToken = refreshToken,
            expiresIn = accessToken.second
        )
    }

    @Transactional(readOnly = true)
    fun getProfile(): AccountResponse {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw InvalidTokenException("Session expired or invalid")

        val user = userRepository.findByEmail(email)
            ?: throw UserNotFoundException(email)

        return AccountResponse(
            firstName = user.profile.firstName,
            lastName = user.profile.lastName,
            role = user.role.toString(),
            createdAt = user.createdAt
        )
    }

    @Transactional(readOnly = true)
    fun checkEmail(request: CheckEmailRequest): AvailabilityResponse {
        val exists = userRepository.findByEmail(requireNotNull(request.email)) != null
        return AvailabilityResponse(
            available = !exists,
            message = if (exists) "Email already exists" else "Email is available"
        )
    }

    private fun parseFullName(fullName: String): Pair<String, String> {
        val parts = fullName.trim().split("\\s+".toRegex(), 2)
        return parts[0] to (parts.getOrNull(1) ?: "")
    }
}