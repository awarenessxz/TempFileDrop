//package com.tempstorage.gateway.filter
//
//import org.slf4j.LoggerFactory
//import org.springframework.http.ResponseCookie
//import org.springframework.security.web.server.csrf.CsrfToken
//import org.springframework.stereotype.Component
//import org.springframework.web.server.ServerWebExchange
//import org.springframework.web.server.WebFilter
//import org.springframework.web.server.WebFilterChain
//import reactor.core.publisher.Mono
//import java.time.Duration
//
//@Component
//class CsrfHelperFilter : WebFilter {
//    companion object {
//        private val logger = LoggerFactory.getLogger(CsrfHelperFilter::class.java)
//        private const val CSRF_COOKIE_NAME = "XSRF-TOKEN"
//    }
//
//    override fun filter(serverWebExchange: ServerWebExchange, webFilterChain: WebFilterChain): Mono<Void> {
//        val key = CsrfToken::class.java.name
//        val csrfToken: Mono<CsrfToken> = serverWebExchange.getAttribute(key) ?: Mono.empty()
//        return csrfToken.doOnSuccess { token ->
//            val cookie = ResponseCookie.from(CSRF_COOKIE_NAME, token.token)
//                    .maxAge(Duration.ofHours(1))
//                    .httpOnly(false)
//                    .path("/")
//                    .build()
//            logger.info("Cookie: $cookie")
//            serverWebExchange.response.cookies.add(CSRF_COOKIE_NAME, cookie)
//        }.then(webFilterChain.filter(serverWebExchange))
//    }
//}