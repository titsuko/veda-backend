package com.titsuko.controller

import com.titsuko.repository.UserRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/users")
class UserController(
    private val userRepository: UserRepository
) {

    data class User(
        val fullName: String,
        val email: String,
        val role: String,
        val createdAt: String,
        val updatedAt: String,
    )

    @GetMapping
    fun getPage(model: Model): String {
        val users = userRepository.findAll().map { user ->
            User(
                fullName = "${user.profile.firstName} ${user.profile.lastName}",
                email = user.email,
                role = user.role.toString(),
                createdAt = user.createdAt.toString().substring(0, 10),
                updatedAt = user.updatedAt.toString().substring(0, 10)
            )

        }
        model.addAttribute("users", users)
        return "users"
    }
}
