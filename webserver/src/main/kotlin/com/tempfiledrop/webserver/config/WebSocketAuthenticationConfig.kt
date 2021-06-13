package com.tempfiledrop.webserver.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99) // https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket-stomp-authentication-token-based
class WebSocketAuthenticationConfig: WebSocketMessageBrokerConfigurer {
    companion object {
        private val logger = LoggerFactory.getLogger(WebSocketAuthenticationConfig::class.java)
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(object: ChannelInterceptor {
            override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
                val accessor = SimpMessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
                if (accessor != null) {
                    if (StompCommand.CONNECT == accessor.command) {
                        val authorizationHeader = accessor.getNativeHeader("Authorization")
                        val bearerToken = authorizationHeader?.get(0)?.replace("Bearer ", "")
                        logger.info("Authorization = $bearerToken")
                        // TODO: Need to implement security
                    }
                }
                return message;
            }
        })
    }
}
