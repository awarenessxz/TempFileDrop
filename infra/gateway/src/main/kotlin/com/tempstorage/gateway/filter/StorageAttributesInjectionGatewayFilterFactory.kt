//package com.tempstorage.gateway.filter
//
//import com.tempstorage.gateway.config.GatewayProperties
//import com.tempstorage.gateway.util.JwtUtils
//import com.tempstorage.gateway.util.RouteValidator
//import com.tempstorage.gateway.util.StorageAttributesUtils
//import org.slf4j.LoggerFactory
//import org.springframework.cloud.gateway.filter.GatewayFilter
//import org.springframework.cloud.gateway.filter.GatewayFilterChain
//import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
//import org.springframework.stereotype.Component
//import org.springframework.web.server.ServerWebExchange
//import reactor.core.publisher.Mono
//import kotlin.properties.Delegates
//
//@Component
//class StorageAttributesInjectionGatewayFilterFactory(
//        private val gatewayProperties: GatewayProperties,
//        private val routeValidator: RouteValidator
//): AbstractGatewayFilterFactory<StorageAttributesInjectionGatewayFilterFactory.Config>(Config::class.java) {
//    companion object {
//        private val logger = LoggerFactory.getLogger(StorageAttributesInjectionGatewayFilterFactory::class.java)
//    }
//
//    class Config {
//        var preFilter by Delegates.notNull<Boolean>()
//    }
//
//    override fun apply(config: Config): GatewayFilter {
//        logger.info("Applying Gateway Filter ${config.preFilter}")
//        return GatewayFilter { exchange: ServerWebExchange, chain: GatewayFilterChain ->
//            val request = exchange.request
//            if (routeValidator.isSecured(request)) {
//                logger.info("Injecting Storage Attributes to ${exchange.request.uri}")
//                if (config.preFilter) {
//                    val bearerToken = JwtUtils.extractBearerToken(request)
//                    bearerToken?.let {
//                        val attributes = gatewayProperties.attributes
//                        val jwtParser = gatewayProperties.jwtKeycloakParser
//                        val jwtUser = JwtUtils.getJwtUser(bearerToken, jwtParser.publicKey, jwtParser.resource, jwtParser.useResourceRoleMappings)
//                        val storageAttributes = StorageAttributesUtils.injectStorageAttributes(jwtUser.roles, attributes)
//                        val injectedRequest = exchange.request.mutate()
//                                .header(StorageAttributesUtils.STORAGE_HEADER_BUCKETS, storageAttributes.first.joinToString(","))
//                                .header(StorageAttributesUtils.STORAGE_HEADER_ROUTING_KEYS, storageAttributes.second.joinToString(","))
//                                .build()
//                        chain.filter(exchange.mutate().request(injectedRequest).build())
//                    }
//                }
//            }
//            chain.filter(exchange).then(Mono.fromRunnable {
//                logger.info("Post GatewayFilter to remove JWT Token")
//            })
//        }
//    }
//
//    override fun shortcutFieldOrder(): List<String> {
//        return listOf("preFilter")
//    }
//}
