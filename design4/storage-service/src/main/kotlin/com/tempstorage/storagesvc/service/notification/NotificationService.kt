package com.tempstorage.storagesvc.service.notification

import com.tempstorage.storagesvc.controller.storage.StorageUploadMetadata
import com.tempstorage.storagesvc.service.storagefiles.StorageFile
import com.tempstorage.storagesvc.service.storagefiles.StorageFileService
import com.tempstorage.storagesvc.service.storageinfo.StorageInfo
import com.tempstorage.storagesvc.service.storageinfo.StorageInfoService
import org.springframework.stereotype.Service

@Service
class NotificationService(
        private val producer: RabbitMQProducer,
        private val storageInfoService: StorageInfoService,
        private val storageFileService: StorageFileService,
) {
    fun triggerUploadNotification(metadata: StorageUploadMetadata, storageInfo: StorageInfo, storageFiles: List<StorageFile>) {
        // update database
        storageInfoService.addStorageInfo(storageInfo)
        storageFileService.saveFilesInfo(storageInfo.id, storageFiles)

        // notify
        producer.sendEventwithHeader(EventType.FILES_UPLOADED, storageInfo, metadata.eventData!!, metadata.eventRoutingKey, true)
    }
}
