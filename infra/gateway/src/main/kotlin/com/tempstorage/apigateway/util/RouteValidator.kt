package com.tempstorage.apigateway.util

import com.tempstorage.apigateway.config.GatewayProperties
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component

@Component
class RouteValidator(
        private val gatewayProps: GatewayProperties
) {
    fun isSecured(request: ServerHttpRequest): Boolean {
        return gatewayProps.whitelist.stream().noneMatch { uri -> request.uri.path.contains(uri) }
    }
}