package com.tempstorage.gateway.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import kotlin.properties.Delegates

@Component
class JwtParserGatewayFilterFactory: AbstractGatewayFilterFactory<JwtParserGatewayFilterFactory.Config>(Config::class.java) {
    companion object {
        private val logger = LoggerFactory.getLogger(JwtParserGatewayFilterFactory::class.java)
    }

    class Config {
        var preFilter by Delegates.notNull<Boolean>()
        var postFilter by Delegates.notNull<Boolean>()
    }

    override fun apply(config: Config): GatewayFilter {
        logger.info("applying gateway filter ${config.preFilter} ${config.postFilter}")
        return GatewayFilter { exchange: ServerWebExchange?, chain: GatewayFilterChain ->
            logger.info("DID YOU GET TRIGGERED? ${config.preFilter} ${config.postFilter}")
            if (config.preFilter) {
                logger.info("Pre GatewayFilter to extract JWT Token")
            }
            chain.filter(exchange).then(Mono.fromRunnable {
                // Post-processing
                if (config.postFilter) {
                    logger.info("Post GatewayFilter to remove JWT Token")
                }
            })
        }
    }

    override fun shortcutFieldOrder(): List<String> {
        return listOf("preFilter", "postFilter")
    }
}
