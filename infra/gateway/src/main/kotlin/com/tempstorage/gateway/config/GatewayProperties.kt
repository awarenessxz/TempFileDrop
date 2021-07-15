package com.tempstorage.gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "gateway")
data class GatewayProperties(
        val whitelist: List<String>,
        val attributes: Map<String, Map<String, List<String>>>,
        val jwtKeycloakParser: JwtKeycloakParser
)

data class JwtKeycloakParser(
        val resource: String,
        val useResourceRoleMappings: Boolean,
        val publicKey: String
)