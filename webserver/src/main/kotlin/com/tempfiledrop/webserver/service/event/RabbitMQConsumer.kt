package com.tempfiledrop.webserver.service.event

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
class RabbitMQConsumer {
    companion object {
        private val logger = LoggerFactory.getLogger(RabbitMQConsumer::class.java)
    }

    @Bean
    fun filesDownloadedChannel(): Consumer<EventMessage> = Consumer {
        logger.info("FILE DOWNLOAD CHANNEL")
        logger.info("Received: {}", it)
    }

    @Bean
    fun filesUploadedChannel(): Consumer<EventMessage> = Consumer {
        logger.info("FILE UPLOAD CHANNEL")
        logger.info("Received: {}", it)
    }
}