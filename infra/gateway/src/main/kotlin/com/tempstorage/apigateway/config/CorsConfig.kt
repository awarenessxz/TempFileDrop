//package com.tempstorage.apigateway.config
//
//import org.springframework.boot.context.properties.EnableConfigurationProperties
//import org.springframework.cloud.context.config.annotation.RefreshScope
//import org.springframework.cloud.gateway.config.GlobalCorsProperties
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.core.Ordered
//import org.springframework.core.annotation.Order
//import org.springframework.web.cors.CorsConfiguration
//import org.springframework.web.cors.reactive.CorsWebFilter
//import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
//import org.springframework.web.reactive.config.CorsRegistry
//import org.springframework.web.reactive.config.EnableWebFlux
//import org.springframework.web.reactive.config.WebFluxConfigurer
//import org.springframework.web.util.pattern.PathPatternParser
//
//@Configuration
//@EnableWebFlux
//@EnableConfigurationProperties(GlobalCorsProperties::class)
//class CorsConfig: WebFluxConfigurer {
//    override fun addCorsMappings(registry: CorsRegistry) {
//        registry.addMapping("/**")
//                .allowedOrigins("*")
//                .allowedMethods("*")
//                .allowedHeaders("*")
//                .allowCredentials(false)
//                .maxAge(3600)
//    }
//
//    @Bean
//    fun corsWebFilter(): CorsWebFilter? {
//        val corsConfiguration = CorsConfiguration()
//        corsConfiguration.allowCredentials = true
//        corsConfiguration.addAllowedHeader("*")
//        corsConfiguration.addAllowedMethod("*")
//        corsConfiguration.addAllowedOrigin("*")
//        val corsConfigurationSource = UrlBasedCorsConfigurationSource()
//        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration)
//        return CorsWebFilter(corsConfigurationSource)
//    }
//
//    @Order(Ordered.HIGHEST_PRECEDENCE)
//    @RefreshScope
//    @Bean
//    fun corsWebFilter(globalCorsProperties: GlobalCorsProperties): CorsWebFilter? {
//        val source = UrlBasedCorsConfigurationSource(PathPatternParser())
//        globalCorsProperties.corsConfigurations.forEach { (k: String?, v: CorsConfiguration?) -> source.registerCorsConfiguration(k!!, v!!) }
//        return CorsWebFilter(source)
//    }
//}