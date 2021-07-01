package com.tempstorage.gateway.filter

import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class LoggingFilter: GlobalFilter, Ordered {
    companion object {
        private val logger = LoggerFactory.getLogger(LoggingFilter::class.java)
    }

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val requestMutated = object: ServerHttpRequestDecorator(exchange.request) {
            override fun getBody(): Flux<DataBuffer> {
                logger.info("Forwarding Request : ${delegate.path}")
                return super.getBody()
            }
        }
        val responseMutated = object: ServerHttpResponseDecorator(exchange.response) {
            override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
                logger.info("Forwarding Response: ${delegate.rawStatusCode}")
                return super.writeWith(body)
            }
        }
        return chain.filter(exchange.mutate().request(requestMutated).response(responseMutated).build())
    }

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE
    }
}