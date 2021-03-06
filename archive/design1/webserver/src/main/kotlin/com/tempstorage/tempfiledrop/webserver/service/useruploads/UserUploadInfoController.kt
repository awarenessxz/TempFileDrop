package com.tempstorage.tempfiledrop.webserver.service.useruploads

import com.tempstorage.tempfiledrop.webserver.exception.ApiException
import com.tempstorage.tempfiledrop.webserver.exception.ErrorCode
import com.tempstorage.tempfiledrop.webserver.service.storagesvcclient.StorageSvcClient
import com.tempstorage.tempfiledrop.webserver.service.userinfo.UserInfoService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users-upload-info")
class UserUploadInfoController(
        @Value("\${tempfiledrop.webserver.bucket-name}") private val tempfiledropBucket: String,
        private val userInfoService: UserInfoService,
        private val uploadedFilesRecordService: UserUploadInfoService,
        private val storageSvcClient: StorageSvcClient
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserUploadInfoController::class.java)
    }

    @GetMapping("/{username}")
    fun getUploadedFilesRecords(@PathVariable("username") username: String): ResponseEntity<List<UserUploadInfoResponse>> {
        logger.info("Retrieving records for $username")
        val user = userInfoService.getUserInfoByUsername(username) ?: throw ApiException("User not found!", ErrorCode.USER_NOT_FOUND, HttpStatus.BAD_REQUEST)
        val records = uploadedFilesRecordService.getUploadedFilesRecordsForUser(user.username)

        // change to bulk request later
        logger.info("${records.size} records found")
        val results: List<UserUploadInfoResponse> = records.map {
            val response = storageSvcClient.getStorageInfoByStorageId(tempfiledropBucket, it.storageId)
            val storageInfo = response.body
            if (!response.statusCode.is2xxSuccessful || storageInfo === null) {
                throw ApiException("Fail to retrieved storage info...", ErrorCode.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR)
            }
            UserUploadInfoResponse(it.id, it.user, storageInfo)
        }
        return ResponseEntity(results, HttpStatus.OK)
    }

    @DeleteMapping("/{recordId}")
    fun deleteUploadedFilesRecords(@PathVariable("recordId") recordId: String): ResponseEntity<Void> {
        logger.info("Deleting record - $recordId")
        val record = uploadedFilesRecordService.getUploadedFilesRecordById(recordId) ?: throw ApiException("Record not found!", ErrorCode.NO_MATCHING_RECORD, HttpStatus.BAD_REQUEST)
        uploadedFilesRecordService.deleteUploadedFilesRecordById(record.id!!)

        // delete record from storage service
        storageSvcClient.deleteFilesInFolder(tempfiledropBucket, record.storageId)

        return ResponseEntity.noContent().build()
    }
}