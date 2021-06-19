package com.tempstorage.tempfiledrop.webserver.util

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

@Component
class JwtAuthenticationFilter(
        @Value("\${storagelib.jwt-parser-keycloak.public-key}") private val publicKey: String,
        @Value("\${storagelib.jwt-parser-keycloak.resource}") private val client: String,
        @Value("\${storagelib.jwt-parser-keycloak.use-resource-role-mappings}") private val useClientRole: Boolean
): GenericFilterBean() {
    companion object {
        private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpServletRequest = request as HttpServletRequest
        val bearerToken = JwtUtils.extractTokenFromRequestHeader(httpServletRequest)
        if (bearerToken == null) {
            logger.info("no valid JWT Token found, uri: ${httpServletRequest.requestURI}")
        } else {
            val authentication = JwtUtils.getAuthentication(bearerToken, publicKey, client, useClientRole)
            SecurityContextHolder.getContext().authentication = authentication
            logger.info("set Authentication to security context for ${authentication.name}, uri: ${httpServletRequest.requestURI}")
        }
        chain.doFilter(request, response)
    }
}