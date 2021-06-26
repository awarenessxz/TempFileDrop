package com.tempstorage.tempfiledrop.webserver.service.storage

import com.tempstorage.tempfiledrop.webserver.service.event.EventMessage

interface StorageService {
    fun processFilesDeletedEvent(eventMessage: EventMessage)
    fun processFilesDownloadedEvent(eventMessage: EventMessage)
    fun processFilesUploadedEvent(eventMessage: EventMessage)
}