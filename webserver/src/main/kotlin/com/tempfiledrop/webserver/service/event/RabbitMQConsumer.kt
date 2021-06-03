package com.tempfiledrop.webserver.service.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.tempfiledrop.webserver.exception.ApiException
import com.tempfiledrop.webserver.exception.ErrorCode
import com.tempfiledrop.webserver.service.useruploads.UserUploadInfo
import com.tempfiledrop.webserver.service.useruploads.UserUploadInfoService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component
class RabbitMQConsumer(
        private val uploadedFilesRecordService: UserUploadInfoService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(RabbitMQConsumer::class.java)
    }

    @Bean
    fun filesDeletedChannel(): Consumer<EventMessage> = Consumer {
        logger.info("Received Files Deleted Event From Storage Service: {}", it)

        // extract event data which we added from frontend first
        val objectMapper = ObjectMapper()
        val data = objectMapper.readValue(it.data, EventDataDelete::class.java)

        // process
        logger.info("Deleting user's upload record...")
        val record = uploadedFilesRecordService.getUploadedFilesRecordById(data.recordId) ?: throw ApiException("Record not found!", ErrorCode.NO_MATCHING_RECORD, HttpStatus.INTERNAL_SERVER_ERROR)
        uploadedFilesRecordService.deleteUploadedFilesRecordById(record.id!!)
    }

    @Bean
    fun filesDownloadedChannel(): Consumer<EventMessage> = Consumer {
        logger.info("Received Files Downloaded Event from Storage Service: {}", it)
    }

    @Bean
    fun filesUploadedChannel(): Consumer<EventMessage> = Consumer {
        logger.info("Received Files Uploaded Event from Storage Service: {}", it)

        // extract event data which we added from frontend first
        val objectMapper = ObjectMapper()
        val data = objectMapper.readValue(it.data, EventDataUpload::class.java)

        // store the storage information into database
        logger.info("Adding <user, upload record> mapping to database -->  <${data.username}, ${it.storageId}>")
        val uploadRecord = UserUploadInfo(null, data.username, it.storageId)
        uploadedFilesRecordService.addUploadedFilesRecord(uploadRecord)
    }
}