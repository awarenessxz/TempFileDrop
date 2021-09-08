package com.tempstorage.storagesvc.service.notification

import com.tempstorage.storagesvc.service.metadata.StorageMetadata
import com.tempstorage.storagesvc.service.metadata.StorageMetadataService
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class NotificationService(
        private val producer: RabbitMQProducer,
        private val storageMetadataService: StorageMetadataService,
        private val simpMessagingTemplate: SimpMessagingTemplate
) {
    fun triggerUploadNotification(storageMetadata: StorageMetadata) {
        val message = NotificationMessage(ZonedDateTime.now(), EventType.FILES_UPLOADED, storageMetadata.bucket, storageMetadata.objectName)
        storageMetadataService.saveStorageMetadata(storageMetadata)                             // update database
        producer.sendEventwithHeader(message)                                                   // notify (rabbitmq)
        simpMessagingTemplate.convertAndSend("/topic/file-uploaded", message)         // notify (websocket)
    }

    fun triggerDeleteNotification(storageMetadata: StorageMetadata) {
        val message = NotificationMessage(ZonedDateTime.now(), EventType.FILES_DELETED, storageMetadata.bucket, storageMetadata.objectName)
        storageMetadataService.deleteStorageMetadataByObjectName(storageMetadata.objectName)    // update database
        producer.sendEventwithHeader(message)                                                   // notify
        simpMessagingTemplate.convertAndSend("/topic/file-deleted", message)          // notify (websocket)
    }

    fun triggerDownloadNotification(storageMetadata: StorageMetadata) {
        val message = NotificationMessage(ZonedDateTime.now(), EventType.FILES_DOWNLOADED, storageMetadata.bucket, storageMetadata.objectName)
        storageMetadataService.reduceDownloadCountByObjectName(storageMetadata.objectName)      // update database
        producer.sendEventwithHeader(message)                                                   // notify
        simpMessagingTemplate.convertAndSend("/topic/file-downloaded", message)       // notify (websocket)
    }
}
