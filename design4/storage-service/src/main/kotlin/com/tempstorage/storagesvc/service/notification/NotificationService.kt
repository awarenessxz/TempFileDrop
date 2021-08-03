package com.tempstorage.storagesvc.service.notification

import com.tempstorage.storagesvc.service.storageinfo.StorageInfo
import com.tempstorage.storagesvc.service.storageinfo.StorageInfoService
import org.springframework.stereotype.Service

@Service
class NotificationService(
        private val producer: RabbitMQProducer,
        private val storageInfoService: StorageInfoService
) {
    fun triggerUploadNotification(uploadedFiles: List<StorageInfo>, eventData: String) {
        uploadedFiles.forEach{ file ->
            storageInfoService.addStorageInfo(file)                                     // update database
            producer.sendEventwithHeader(EventType.FILES_UPLOADED, file, eventData)     // notify
        }
    }

    fun triggerDeleteNotification(storageInfo: StorageInfo, eventData: String) {
        storageInfoService.deleteStorageInfoById(storageInfo.id)                        // update database
        producer.sendEventwithHeader(EventType.FILES_DELETED, storageInfo, eventData)   // notify
    }

    fun triggerDownloadNotification(storageInfo: StorageInfo, eventData: String) {
        storageInfoService.reduceDownloadCountById(storageInfo.id)                          // update database
        producer.sendEventwithHeader(EventType.FILES_DOWNLOADED, storageInfo, eventData)    // notify
    }
}
