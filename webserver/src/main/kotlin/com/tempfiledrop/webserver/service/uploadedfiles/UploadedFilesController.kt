package com.tempfiledrop.webserver.service.uploadedfiles

import com.tempfiledrop.webserver.config.ServerProperties
import com.tempfiledrop.webserver.exception.ApiException
import com.tempfiledrop.webserver.exception.ErrorCode
import com.tempfiledrop.webserver.service.filestorage.FileStorageController
import com.tempfiledrop.webserver.service.storagesvcclient.StorageSvcClientImpl
import com.tempfiledrop.webserver.service.userinfo.UserInfoServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime
import java.util.*

@RestController
@RequestMapping("/api/uploaded-files")
class UploadedFilesController(
        private val serverProperties: ServerProperties,
        private val userInfoService: UserInfoServiceImpl,
        private val uploadedFilesRecordService: UploadedFilesServiceImpl,
        private val storageSvcClient: StorageSvcClientImpl
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UploadedFilesController::class.java)
    }

    @GetMapping("/{username}")
    fun getUploadedFilesRecords(@PathVariable("username") username: String): ResponseEntity<List<UploadedFiles>> {
        logger.info("Retrieving records for $username")
        val user = userInfoService.getUserInfoByUsername(username) ?: throw ApiException("User not found!", ErrorCode.USER_NOT_FOUND, HttpStatus.BAD_REQUEST)
        val records = uploadedFilesRecordService.getUploadedFilesRecordsForUser(user.username)
        logger.info("${records.size} records found")
        return ResponseEntity(records, HttpStatus.OK)
    }

    @DeleteMapping("/{recordId}")
    fun deleteUploadedFilesRecords(@PathVariable("recordId") recordId: String): ResponseEntity<Void> {
        logger.info("Deleting record - $recordId")
        val record = uploadedFilesRecordService.getUploadedFilesRecordById(recordId) ?: throw ApiException("Record not found!", ErrorCode.NO_MATCHING_RECORD, HttpStatus.BAD_REQUEST)
        uploadedFilesRecordService.deleteUploadedFilesRecordById(record.id!!)

        // delete record from storage service
        storageSvcClient.deleteFilesInFolder(serverProperties.bucketName, record.storageId)

        return ResponseEntity.noContent().build()
    }

    @GetMapping("/download-info/{storageId}")
    fun getStorageInformationForDownload(@PathVariable("storageId") storageId: String): ResponseEntity<UploadedFiles> {
        logger.info("Receiving request to get download information for $storageId")

        // verify if files exists on server
        val storageSvcResponse = storageSvcClient.getStorageInfoByStorageId(serverProperties.bucketName, storageId)
        val storageInfoResponse = storageSvcResponse.body
        logger.info("Response ==> $storageInfoResponse")
        if (!storageSvcResponse.statusCode.is2xxSuccessful || storageInfoResponse == null || storageInfoResponse.files.isEmpty()) {
            throw ApiException("Fail to retrieve download information.", ErrorCode.NO_MATCHING_RECORD, HttpStatus.BAD_REQUEST)
        }

        // verify that file can still be downloaded
        val uploadedFilesRecord = uploadedFilesRecordService.getUploadedFilesRecordByStorageId(storageId) ?: throw ApiException("Record not found!", ErrorCode.NO_MATCHING_RECORD, HttpStatus.BAD_REQUEST)
        if (uploadedFilesRecord.numOfDownloadsLeft <= 0 || uploadedFilesRecord.expiryDatetime.isBefore(ZonedDateTime.now())) {
            uploadedFilesRecordService.deleteUploadedFilesRecordById(uploadedFilesRecord.id!!)
            storageSvcClient.deleteFilesInFolder(serverProperties.bucketName, uploadedFilesRecord.storageId)
            throw ApiException("Record not found!", ErrorCode.NO_MATCHING_RECORD, HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity(uploadedFilesRecord, HttpStatus.OK)
    }
}