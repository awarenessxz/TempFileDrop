package com.tempfiledrop.webserver.service.storage

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.tempfiledrop.webserver.exception.ApiException
import com.tempfiledrop.webserver.exception.ErrorCode
import com.tempfiledrop.webserver.service.event.EventDataDelete
import com.tempfiledrop.webserver.service.event.EventDataUpload
import com.tempfiledrop.webserver.service.event.EventMessage
import com.tempfiledrop.webserver.service.useruploads.UserUploadInfo
import com.tempfiledrop.webserver.service.useruploads.UserUploadInfoService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class StorageServiceImpl(
        private val uploadedFilesRecordService: UserUploadInfoService
) : StorageService {
    companion object {
        private val logger = LoggerFactory.getLogger(StorageServiceImpl::class.java)
    }

    override fun processFilesDeletedEvent(eventMessage: EventMessage) {
        // extract event data which we added from frontend first
        val objectMapper = ObjectMapper().registerKotlinModule()
        val data = objectMapper.readValue(eventMessage.data, EventDataDelete::class.java)

        // process
        logger.info("Deleting user's upload record...")
        val record = uploadedFilesRecordService.getUploadedFilesRecordById(data.recordId) ?: throw ApiException("Record not found!", ErrorCode.NO_MATCHING_RECORD, HttpStatus.INTERNAL_SERVER_ERROR)
        uploadedFilesRecordService.deleteUploadedFilesRecordById(record.id!!)
    }

    override fun processFilesDownloadedEvent(eventMessage: EventMessage) {

    }

    override fun processFilesUploadedEvent(eventMessage: EventMessage) {
        // extract event data which we added from frontend first
        val objectMapper = ObjectMapper().registerKotlinModule()
        val data = objectMapper.readValue(eventMessage.data, EventDataUpload::class.java)

        // store the storage information into database
        logger.info("Adding <user, upload record> mapping to database -->  <${data.username}, ${eventMessage.storageId}>")
        val uploadRecord = UserUploadInfo(null, data.username, eventMessage.storageId)
        uploadedFilesRecordService.addUploadedFilesRecord(uploadRecord)
    }
}