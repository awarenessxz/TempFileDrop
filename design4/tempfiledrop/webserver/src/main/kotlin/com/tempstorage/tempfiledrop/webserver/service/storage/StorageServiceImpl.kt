package com.tempstorage.tempfiledrop.webserver.service.storage

import com.tempstorage.tempfiledrop.webserver.service.event.NotificationMessage
import com.tempstorage.tempfiledrop.webserver.service.useruploads.UserUploadInfo
import com.tempstorage.tempfiledrop.webserver.service.useruploads.UserUploadInfoService
import com.tempstorage.tempfiledrop.webserver.util.FileUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class StorageServiceImpl(
        private val uploadedFilesRecordService: UserUploadInfoService
) : StorageService {
    companion object {
        private val logger = LoggerFactory.getLogger(StorageServiceImpl::class.java)
    }

    override fun processFilesDeletedEvent(eventMessage: NotificationMessage) {
        logger.info("Deleting user's upload record... ${eventMessage.objectName}")
        uploadedFilesRecordService.deleteUploadedFilesRecordByObjectName(eventMessage.objectName)
    }

    override fun processFilesDownloadedEvent(eventMessage: NotificationMessage) {
    }

    override fun processFilesUploadedEvent(eventMessage: NotificationMessage) {
        val username = FileUtil.getUsernameFromObjectName(eventMessage.objectName)
        logger.info("Adding <user, upload record> mapping to database -->  <$username, ${eventMessage.objectName}>")
        uploadedFilesRecordService.addUploadedFilesRecord(UserUploadInfo(username, eventMessage.objectName))
    }
}