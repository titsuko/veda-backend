package com.titsuko.service

import com.titsuko.dto.request.UpdateUserRequest
import com.titsuko.dto.response.UserResponse
import com.titsuko.exception.UserNotFoundException
import com.titsuko.model.User
import com.titsuko.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class UserService(
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        .withZone(ZoneId.systemDefault())

    @Transactional(readOnly = true)
    fun getAllUsers(limit: Pageable): Page<UserResponse> {
        return userRepository.findAll(limit).map { mapToResponse(it) }
    }

    @Transactional
    fun getUserForEdit(id: Long): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException("User not found") }
        return mapToResponse(user)
    }

    @Transactional
    fun updateUser(id: Long, request: UpdateUserRequest, editorEmail: String) {
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException("User not found") }

        if (user.email == editorEmail) {
            logger.warn("Admin $editorEmail tried to edit their own account via User Panel")
            throw AccessDeniedException("You can't edit your account from the admin panel")
        }

        user.email = request.email
        user.profile.firstName = request.firstName
        user.profile.lastName = request.lastName

        userRepository.save(user)
        logger.info("User $id updated by $editorEmail")
    }

    @Transactional
    fun deleteUser(id: Long, editorEmail: String) {
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException("User not found") }

        if (user.email == editorEmail) {
            throw AccessDeniedException("You cannot delete your own account")
        }

        userRepository.delete(user)
        logger.info("User $id deleted by $editorEmail")
    }

    private fun mapToResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id,
            fullName = "${user.profile.firstName} ${user.profile.lastName}",
            email = user.email,
            role = user.role.toString(),

            createdAt = user.createdAt?.let { dateFormatter.format(it) } ?: "",
            updatedAt = user.updatedAt?.let { dateFormatter.format(it) } ?: ""
        )
    }
}