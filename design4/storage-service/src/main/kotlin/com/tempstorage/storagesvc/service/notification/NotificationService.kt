package com.tempstorage.storagesvc.service.notification

import com.tempstorage.storagesvc.service.metadata.StorageMetadata
import com.tempstorage.storagesvc.service.metadata.StorageMetadataService
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class NotificationService(
        private val producer: RabbitMQProducer,
        private val storageMetadataService: StorageMetadataService,
        private val simpMessagingTemplate: SimpMessagingTemplate
) {
    fun triggerUploadNotification(storageMetadata: StorageMetadata) {
        storageMetadataService.saveStorageMetadata(storageMetadata)                             // update database
        producer.sendEventwithHeader(EventType.FILES_UPLOADED, storageMetadata)                 // notify (rabbitmq)
        simpMessagingTemplate.convertAndSend("/topic/file-uploaded", storageMetadata) // notify (websocket)
    }

    fun triggerDeleteNotification(storageMetadata: StorageMetadata) {
        storageMetadataService.deleteStorageMetadataByObjectName(storageMetadata.objectName)    // update database
        producer.sendEventwithHeader(EventType.FILES_DELETED, storageMetadata)                  // notify
        simpMessagingTemplate.convertAndSend("/topic/file-deleted", storageMetadata)  // notify (websocket)
    }

    fun triggerDownloadNotification(storageMetadata: StorageMetadata) {
        storageMetadataService.reduceDownloadCountByObjectName(storageMetadata.objectName)        // update database
        producer.sendEventwithHeader(EventType.FILES_DOWNLOADED, storageMetadata)                 // notify
        simpMessagingTemplate.convertAndSend("/topic/file-downloaded", storageMetadata) // notify (websocket)
    }
}
