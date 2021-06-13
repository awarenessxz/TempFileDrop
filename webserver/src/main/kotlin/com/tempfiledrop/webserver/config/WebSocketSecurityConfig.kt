package com.tempfiledrop.webserver.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.SimpMessageType.CONNECT
import org.springframework.messaging.simp.SimpMessageType.DISCONNECT
import org.springframework.messaging.simp.SimpMessageType.HEARTBEAT
import org.springframework.messaging.simp.SimpMessageType.UNSUBSCRIBE
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer

@Configuration
class WebSocketSecurityConfig: AbstractSecurityWebSocketMessageBrokerConfigurer() {
    override fun configureInbound(messages: MessageSecurityMetadataSourceRegistry?) {
       if (messages !== null) {
           messages
                   .simpTypeMatchers(CONNECT, UNSUBSCRIBE, DISCONNECT, HEARTBEAT).permitAll()
                   .simpDestMatchers("/app/**", "/topic/**").permitAll()
                   .simpSubscribeDestMatchers("/topic/**").permitAll()
                   .anyMessage().denyAll()
       }
    }

    override fun sameOriginDisabled(): Boolean {
        return true
    }
}
