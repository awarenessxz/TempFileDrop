package com.tempstorage.apigateway.handler

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.security.Principal

@Component
class UserHandler {
    companion object {
        private val logger = LoggerFactory.getLogger(UserHandler::class.java)
    }

    fun getUser(request: ServerRequest): Mono<ServerResponse> {
        logger.info("Request Headers == ${request.headers()}")
        return request.principal()
                .map(Principal::getName)
                .flatMap { username -> ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).body(BodyInserters.fromValue("Hello, $username!")) }
    }
}