package com.tempfiledrop.webserver.service.storage

import com.tempfiledrop.webserver.service.event.EventMessage

interface StorageService {
    fun processFilesDeletedEvent(eventMessage: EventMessage)
    fun processFilesDownloadedEvent(eventMessage: EventMessage)
    fun processFilesUploadedEvent(eventMessage: EventMessage)
}