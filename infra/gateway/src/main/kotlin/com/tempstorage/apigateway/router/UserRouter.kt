package com.tempstorage.apigateway.router

import com.tempstorage.apigateway.handler.UserHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.*

@Configuration
class UserRouter {
    @Bean
    fun router(userHandler: UserHandler): RouterFunction<ServerResponse> {
        return RouterFunctions
                .route(RequestPredicates.GET("/auth/user"), userHandler::getUser)
    }
}
