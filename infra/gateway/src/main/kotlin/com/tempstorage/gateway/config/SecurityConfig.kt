package com.tempstorage.gateway.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode
import reactor.core.publisher.Mono
import java.net.URI

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
        private val gatewayProps: GatewayProperties,
        private val clientRegistrationRepository: ReactiveClientRegistrationRepository,
        @Value("\${server.port}") private val serverPort: String
) {
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.cors()
        http.oauth2Login(withDefaults()) // Authenticate through configured OpenID Provider
//        http.csrf { csrf -> csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse()) } // so that react app can get XSRF-TOKEN
        http.authorizeExchange()
                .pathMatchers(*gatewayProps.whitelist.toTypedArray()).permitAll()
                .anyExchange().authenticated()
        http.oauth2ResourceServer { obj -> obj.jwt() }  // Token Validation
        http.headers().frameOptions().mode(Mode.SAMEORIGIN)
//        http.requestCache().requestCache(NoOpServerRequestCache.getInstance()) // Stateless Session
//        http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // Stateless Session
        http.csrf().disable() // Disable CSRF in the gateway to prevent conflicts with proxied service CSRF
        http.logout().logoutSuccessHandler(oidcLogoutSuccessHandler())
        return http.build()
    }

    private fun oidcLogoutSuccessHandler(): ServerLogoutSuccessHandler {
        return object : OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository) {
            override fun onLogoutSuccess(exchange: WebFilterExchange, authentication: Authentication): Mono<Void> {
                // https://stackoverflow.com/q/15988323/1098564
                // logout was called and proxied, let's default redirection to "origin"
                val origin = exchange.exchange.request.headers[HttpHeaders.ORIGIN]
                // https://stackoverflow.com/q/22397072/1098564
                setPostLogoutRedirectUri(URI.create(if (origin!!.isEmpty() || "null" == origin[0]) "http://localhost:$serverPort" else origin[0]))
                return super.onLogoutSuccess(exchange, authentication)
            }
        }
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
