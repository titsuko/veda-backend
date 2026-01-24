package com.titsuko.server.controller

import com.titsuko.server.service.SessionService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sessions")
class SessionController(
    private val sessionService: SessionService
)