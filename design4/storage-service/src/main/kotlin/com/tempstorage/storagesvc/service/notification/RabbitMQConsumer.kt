package com.tempstorage.storagesvc.service.notification

import io.minio.messages.EventType
import io.minio.messages.NotificationRecords
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
class RabbitMQConsumer(
        private val notificationService: NotificationService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(RabbitMQConsumer::class.java)
    }

    @Bean
    fun minioBucketEventsChannel(): Consumer<NotificationRecords> = Consumer {
        logger.info("Receiving Events from Minio Cluster....")
        val events = it.events()
        events.forEach { event ->
            logger.info("Event Type => ${event.eventType()}")
            logger.info("Event Details => ${event.bucketName()} ${event.objectName()}")
            when(event.eventType()) {
                EventType.OBJECT_CREATED_PUT, EventType.OBJECT_CREATED_POST -> {
                    logger.info("File Have been Uploaded!")
                }
                EventType.OBJECT_ACCESSED_GET -> {
                    logger.info("File Have been Downloaded!")
                }
                EventType.OBJECT_REMOVED_DELETE -> {
                    logger.info("File have been deleted")
                }
                else -> logger.info("Not processed!!")
            }
        }
    }
}