package com.tempstorage.storagesvc.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig: WebSocketMessageBrokerConfigurer {
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // register STOMP endpoints (with sockjs fallback)
        registry.addEndpoint("/storage-ws").setAllowedOrigins("*")
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // Any incoming messages to this prefix will be redirected to the same outgoing prefix which will be received by all subscribers
        registry.enableSimpleBroker("/topic")
        // Any incoming messages to this prefix will be send to the controller (via @MessageMapping) before routing to the simple broker
        registry.setApplicationDestinationPrefixes("/app")
    }
}
