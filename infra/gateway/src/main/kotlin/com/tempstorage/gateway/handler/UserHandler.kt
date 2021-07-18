package com.tempstorage.gateway.handler

import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URI

@Component
class UserHandler {
    companion object {
        private val logger = LoggerFactory.getLogger(UserHandler::class.java)
    }

    fun getUser(request: ServerRequest): Mono<ServerResponse> {
        val context = SecurityContextHolder.getContext()
        val authentication = context.authentication
        logger.info("Authentication --> $authentication")
        logger.info("Request Headers --> ${request.headers()}")
        return request.principal()
                .map { principal -> logger.info("Principal => $principal")}
                .flatMap { principal -> ServerResponse.ok().body(BodyInserters.fromValue(principal)) }
//        return request.principal()
//                .filter { principal -> principal is OAuth2AuthenticationToken }
//                .cast(OAuth2AuthenticationToken::class.java)
//                .map { auth -> logger.info("Auth => $auth") }
//                .flatMap { auth -> ServerResponse.ok().body(BodyInserters.fromValue(auth)) }
    }

    fun loginTempFileDrop(request: ServerRequest): Mono<ServerResponse> {
        logger.info("YOU ARE TRYING TO LOGIN!!! ${request.uri().path}")
        return ServerResponse.temporaryRedirect(URI.create("/tempfiledrop/")).build();
    }

    fun loginStorageConsole(request: ServerRequest): Mono<ServerResponse> {
        logger.info("YOU ARE TRYING TO LOGIN!!! ${request.uri().path}")
        return ServerResponse.temporaryRedirect(URI.create("/console/")).build();
    }
}
