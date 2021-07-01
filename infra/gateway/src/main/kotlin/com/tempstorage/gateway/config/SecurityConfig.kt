package com.tempstorage.gateway.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
        private val gatewayProps: GatewayProperties
) {
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.cors()
        http.authorizeExchange()
                .pathMatchers(*gatewayProps.whitelist.toTypedArray()).permitAll()
                .anyExchange().authenticated()
        http.oauth2Login(withDefaults()) // Authenticate through configured OpenID Provider
        http.oauth2ResourceServer { obj -> obj.jwt() }  // Token Validation
        http.headers().frameOptions().mode(Mode.SAMEORIGIN)
//        http.requestCache().requestCache(NoOpServerRequestCache.getInstance()) // Stateless Session
//        http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // Stateless Session
        http.csrf().disable() // Disable CSRF in the gateway to prevent conflicts with proxied service CSRF
        return http.build()
    }

//    @Bean
//    fun corsConfigurationSource(): CorsConfigurationSource {
//        val corsConfig = CorsConfiguration()
//        corsConfig.allowedOrigins = listOf("*")
//        corsConfig.maxAge = 3600L
//        corsConfig.addAllowedMethod("*")
//        corsConfig.addAllowedHeader("*")
//        val source = UrlBasedCorsConfigurationSource()
//        source.registerCorsConfiguration("/**", corsConfig)
//        return source
//    }
}
