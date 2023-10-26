package com.example.todolist.configuration.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val tokenProvider: TokenProvider,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = parseBearerToken(request)
        val user = parseUserSpecification(token)
        UsernamePasswordAuthenticationToken.authenticated(user, token, user.authorities)
            .apply { details = WebAuthenticationDetails(request) }
            .also { SecurityContextHolder.getContext().authentication = it }

        filterChain.doFilter(request, response)
    }

    private fun parseBearerToken(request: HttpServletRequest) = request.getHeader(HttpHeaders.AUTHORIZATION)
        .takeIf { it?.startsWith("Bearer ", true) ?: false }?.substring(7)

    private fun parseUserSpecification(token: String?) = (
            token?.takeIf { it.isNotEmpty() }
                ?.let { tokenProvider.validateTokenAndGetSubject(it) }
                ?: "anonymous"
            )
        .let { User(it, "", listOf(SimpleGrantedAuthority("user"))) }
}
