package com.tempstorage.gateway.util

import com.tempstorage.gateway.config.GatewayProperties
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