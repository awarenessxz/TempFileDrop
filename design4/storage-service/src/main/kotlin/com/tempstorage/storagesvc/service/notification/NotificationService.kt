package com.tempstorage.storagesvc.service.notification

import com.tempstorage.storagesvc.service.metadata.StorageMetadata
import com.tempstorage.storagesvc.service.metadata.StorageMetadataService
import org.springframework.stereotype.Service

@Service
class NotificationService(
        private val producer: RabbitMQProducer,
        private val storageMetadataService: StorageMetadataService
) {
    fun triggerUploadNotification(storageMetadata: StorageMetadata) {
        storageMetadataService.saveStorageMetadata(storageMetadata)                 // update database
        producer.sendEventwithHeader(EventType.FILES_UPLOADED, storageMetadata)     // notify
    }

    fun triggerDeleteNotification(storageMetadata: StorageMetadata) {
        storageMetadataService.deleteStorageMetadataByObjectName(storageMetadata.objectName)  // update database
        producer.sendEventwithHeader(EventType.FILES_DELETED, storageMetadata)                // notify
    }

    fun triggerDownloadNotification(storageMetadata: StorageMetadata) {
        storageMetadataService.reduceDownloadCountByObjectName(storageMetadata.objectName)   // update database
        producer.sendEventwithHeader(EventType.FILES_DOWNLOADED, storageMetadata)            // notify
    }
}
