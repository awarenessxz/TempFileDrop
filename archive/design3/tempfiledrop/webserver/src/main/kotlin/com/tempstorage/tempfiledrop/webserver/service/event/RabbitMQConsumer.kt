package com.tempstorage.tempfiledrop.webserver.service.event

import com.tempstorage.tempfiledrop.webserver.service.storage.StorageService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
class RabbitMQConsumer(
        private val storageService: StorageService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(RabbitMQConsumer::class.java)
    }

    @Bean
    fun storageSvcChannel(): Consumer<EventMessage> = Consumer {
        logger.info("Received Event (${it.eventType}) From Storage Service: $it")
        when (EventType.valueOf(it.eventType)) {
            EventType.FILES_DELETED -> storageService.processFilesDeletedEvent(it)
            EventType.FILES_DOWNLOADED -> storageService.processFilesDownloadedEvent(it)
            EventType.FILES_UPLOADED -> storageService.processFilesUploadedEvent(it)
        }
    }
}