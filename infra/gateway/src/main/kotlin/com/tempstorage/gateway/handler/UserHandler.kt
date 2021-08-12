package com.tempstorage.gateway.handler

import com.tempstorage.gateway.model.JwtUser
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URI

@Component
class UserHandler {
    companion object {
        private val logger = LoggerFactory.getLogger(UserHandler::class.java)
    }

    // This returns empty string when user is not login
    fun getUser(request: ServerRequest): Mono<ServerResponse> {
        logger.info("Request Headers --> ${request.headers()}")

        return ReactiveSecurityContextHolder.getContext()
                .map { context ->
                    logger.info("Token: ${context.authentication}")
                    val principal = (context.authentication as OAuth2AuthenticationToken).principal
                    val token = principal as OidcUser
                    logger.info("Token: ${token.idToken.tokenValue}")
                    logger.info("Principal : $principal")
                    JwtUser(
                            principal.getAttribute("given_name") ?: principal.name,
                            principal.name,
                            principal.getAttribute("client-roles") ?: ArrayList(),
                            token.idToken.tokenValue
                    )
                }
                .flatMap { user -> ServerResponse.ok().bodyValue(user) }
                .switchIfEmpty(ServerResponse.notFound().build())
//        return request.principal()
//                .map { principal -> logger.info("Principal => $principal") }
//                .flatMap { principal -> ServerResponse.ok().body(BodyInserters.fromValue(principal)) }
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
