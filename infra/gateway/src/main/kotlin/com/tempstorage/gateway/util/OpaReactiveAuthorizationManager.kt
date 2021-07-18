package com.tempstorage.gateway.util

import org.slf4j.LoggerFactory
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.ReactiveAuthorizationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authorization.AuthorizationContext
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class OpaReactiveAuthorizationManager: ReactiveAuthorizationManager<AuthorizationContext> {
    companion object {
        private val logger = LoggerFactory.getLogger(OpaReactiveAuthorizationManager::class.java)
    }

    override fun check(authentication: Mono<Authentication>, context: AuthorizationContext): Mono<AuthorizationDecision> {
        val request = context.exchange.request
        logger.info("OPA Auth Mgr -- Auhorizing ${request.method} to ${request.path}")
        return Mono.just(AuthorizationDecision(true))
    }
}