package com.tempfiledrop.webserver.service.event

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
class RabbitMQConsumer {
    companion object {
        private val logger = LoggerFactory.getLogger(RabbitMQConsumer::class.java)
    }

    @Bean
    fun filesDownloadedChannel(): Consumer<EventMessage> = Consumer {
        logger.info("Received: {}", it)
    }

    @Bean
    fun filesUploadedChannel(): Consumer<EventMessage> = Consumer {
        logger.info("Received: {}", it)
    }
}