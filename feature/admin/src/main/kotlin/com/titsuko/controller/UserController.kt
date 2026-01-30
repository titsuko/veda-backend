package com.titsuko.controller

import com.titsuko.dto.request.UpdateUserRequest
import com.titsuko.service.UserService
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.security.Principal

@Controller
@RequestMapping("/admin/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping
    fun getPage(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") limit: Int,
        model: Model
    ): String {
        val pageable = PageRequest.of(page, limit)
        val usersPage = userService.getAllUsers(pageable)

        model.addAttribute("usersPage", usersPage)
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", usersPage.totalPages)
        return "users"
    }

    @PostMapping("/{id}/update")
    fun updateUser(
        @PathVariable id: Long,
        @ModelAttribute form: UpdateUserRequest,
        principal: Principal
    ): String {
        try {
            userService.updateUser(id, form, principal.name)
        } catch (e: Exception) {
            return "redirect:/admin/users?error=${e.message}"
        }
        return "redirect:/admin/users"
    }

    @PostMapping("/{id}/delete")
    fun deleteUser(@PathVariable id: Long, principal: Principal): String {
        try {
            userService.deleteUser(id, principal.name)
        } catch (e: Exception) {
            return "redirect:/admin/users?error=${e.message}"
        }
        return "redirect:/admin/users"
    }
}
