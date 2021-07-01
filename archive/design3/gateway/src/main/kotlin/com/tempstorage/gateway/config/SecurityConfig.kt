package com.tempstorage.gateway.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
        private val gatewayProps: GatewayProperties
) {
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.authorizeExchange()
                .pathMatchers(*gatewayProps.whitelist.toTypedArray()).permitAll()
                .anyExchange().authenticated()
        http.oauth2Login(withDefaults()) // Authenticate through configured OpenID Provider
        http.oauth2ResourceServer { obj -> obj.jwt() }  // Token Validation
        http.csrf().disable() // Disable CSRF in the gateway to prevent conflicts with proxied service CSRF
        return http.build()
    }
}