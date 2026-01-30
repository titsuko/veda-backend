package com.titsuko.security

import com.titsuko.security.filter.AdminFilter
import com.titsuko.security.filter.JwtAuthFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder =
        BCryptPasswordEncoder(10)

    @Bean
    fun filterChain(
        http: HttpSecurity,
        jwtAuthFilter: JwtAuthFilter,
        adminFilter: AdminFilter
    ): SecurityFilterChain {
        return http
            .csrf { csrf -> csrf.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/admin/**", "/css/**", "/js/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/accounts").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/accounts/check-email").permitAll()
                    // session endpoints:
                    .requestMatchers(HttpMethod.POST, "/api/sessions").permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/api/sessions/logout").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/sessions/refresh").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(adminFilter, JwtAuthFilter::class.java)
            .exceptionHandling { configurer ->
                configurer.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .build()
    }
}