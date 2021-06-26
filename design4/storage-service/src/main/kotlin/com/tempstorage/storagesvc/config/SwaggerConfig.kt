package com.tempstorage.storagesvc.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(info = Info(title = "Centralized Storage Service APIs", version = "v1"))
@SecurityScheme(
        name = "bearer-token",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
class SwaggerConfig {
//    // global set @SecurityRequirements for all methods
//    @Bean
//    fun customOpenAPI(): OpenAPI {
//        val securitySchemeName = "bearer-token"
//        return OpenAPI()
//                .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
//                .components(Components().addSecuritySchemes(securitySchemeName,
//                        SecurityScheme().name(securitySchemeName).type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
//                .info(Info().title("Centralized Storage Service APIs").version("v1.0.0"))
//    }
}
