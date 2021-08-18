package com.tempstorage.tempfiledrop.webserver.service.storage

import com.tempstorage.tempfiledrop.webserver.service.event.NotificationMessage

interface StorageService {
    fun processFilesDeletedEvent(eventMessage: NotificationMessage)
    fun processFilesDownloadedEvent(eventMessage: NotificationMessage)
    fun processFilesUploadedEvent(eventMessage: NotificationMessage)
}