package com.tempstorage.storagesvc.service.notification

import com.tempstorage.storagesvc.service.metadata.StorageMetadata
import io.minio.messages.EventType
import io.minio.messages.NotificationRecords
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URLDecoder
import java.time.ZonedDateTime
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
            val userMetadata = event.userMetadata()
            val storageMetadata = StorageMetadata(
                    event.bucketName(),
                    URLDecoder.decode(event.objectName(), "UTF-8"),
                    "",
                    event.objectSize(),
                    userMetadata[StorageMetadata.MAX_DOWNLOAD_COUNT]!!.toInt(),
                    ZonedDateTime.parse(userMetadata[StorageMetadata.EXPIRY_PERIOD]),
                    false
            )
            when(event.eventType()) {
                EventType.OBJECT_CREATED_PUT, EventType.OBJECT_CREATED_POST -> {
                    logger.info("[${event.eventType()}] File Have been Uploaded! $storageMetadata")
                    notificationService.triggerUploadNotification(storageMetadata)
                }
                EventType.OBJECT_ACCESSED_GET -> {
                    logger.info("[${event.eventType()}] File Have been Downloaded! $storageMetadata")
                    notificationService.triggerDownloadNotification(storageMetadata)
                }
                EventType.OBJECT_REMOVED_DELETE -> {
                    logger.info("[${event.eventType()}] File have been deleted $storageMetadata")
                    notificationService.triggerDeleteNotification(storageMetadata)
                }
                else -> logger.info("[${event.eventType()}] Event is not processed!!")
            }
        }
    }
}