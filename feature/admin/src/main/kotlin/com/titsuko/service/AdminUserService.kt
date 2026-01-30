package com.titsuko.service

import com.titsuko.dto.request.UpdateUserRequest
import com.titsuko.dto.response.UserResponse
import com.titsuko.exception.UserNotFoundException
import com.titsuko.model.User
import com.titsuko.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminUserService(
    private val userRepository: UserRepository
) {

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
    fun updateUser(id: Long, request: UpdateUserRequest, email: String) {
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException("User not found") }

        if (user.email == email) {
            throw AccessDeniedException("You can't edit your account from the admin panel")
        }

        user.email = request.email
        user.profile.firstName = request.firstName
        user.profile.lastName = request.lastName

        userRepository.save(user)
    }

    @Transactional
    fun deleteUser(id: Long, email: String) {
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException("User not found") }

        if (user.email == email) {
            throw AccessDeniedException("You cannot delete your own account")
        }

        userRepository.delete(user)
    }

    private fun mapToResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id,
            fullName = "${user.profile.firstName} ${user.profile.lastName}",
            email = user.email,
            role = user.role.toString(),
            createdAt = user.createdAt.toString().substring(0, 10),
            updatedAt = user.updatedAt.toString().substring(0, 10),
        )
    }
}
