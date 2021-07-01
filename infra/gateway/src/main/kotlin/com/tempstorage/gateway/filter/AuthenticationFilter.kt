package com.tempstorage.gateway.filter

import com.tempstorage.gateway.util.JwtUtils
import com.tempstorage.gateway.util.RouteValidator
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class AuthenticationFilter(
        private val routeValidator: RouteValidator
): GatewayFilter {
    companion object {
        private val logger = LoggerFactory.getLogger(AuthenticationFilter::class.java)
    }

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        if (routeValidator.isSecured(request)) {
            val bearerToken = JwtUtils.extractTokenFromRequestHeader(request)
            logger.info("Header ==> $bearerToken")
        } else {
            logger.info("${request.uri} is an Open API")
        }
        return chain.filter(exchange)
    }
}