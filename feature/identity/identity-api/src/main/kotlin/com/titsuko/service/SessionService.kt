package com.titsuko.service

import com.titsuko.dto.request.LoginRequest
import com.titsuko.dto.request.RefreshRequest
import com.titsuko.dto.response.AuthResponse
import com.titsuko.exception.InvalidCredentialsException
import com.titsuko.exception.UserNotFoundException
import com.titsuko.repository.UserRepository
import com.titsuko.security.HashEncoder
import com.titsuko.security.JwtService
import com.titsuko.security.RefreshService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SessionService(
    private val userRepository: UserRepository,
    private val hashEncoder: HashEncoder,
    private val jwtService: JwtService,
    private val refreshService: RefreshService
) {

    private val logger = LoggerFactory.getLogger(SessionService::class.java)

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        val email = requireNotNull(request.email)
        val user = userRepository.findByEmail(email)
            ?: throw UserNotFoundException(email)

        if (!hashEncoder.matches(requireNotNull(request.password), user.password)) {
            logger.warn("Failed login attempt for user: $email")
            throw InvalidCredentialsException()
        }

        val accessToken = jwtService.generateAccessToken(user.email)
        val refreshToken = refreshService.createToken(user)

        logger.info("User logged in: $email")

        return AuthResponse(
            accessToken.first,
            refreshToken,
            expiresIn = accessToken.second
        )
    }

    @Transactional
    fun logout(request: RefreshRequest) {
        refreshService.revokeToken(requireNotNull(request.token))
    }

    @Transactional
    fun refresh(request: RefreshRequest): AuthResponse {
        val (user, refreshToken) = refreshService.rotateToken(requireNotNull(request.token))
        val accessToken = jwtService.generateAccessToken(user.email)

        return AuthResponse(
            accessToken.first,
            refreshToken,
            expiresIn = accessToken.second
        )
    }
}