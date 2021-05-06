package com.tempfiledrop.webserver.service.useruploads

import com.tempfiledrop.webserver.config.ServerProperties
import com.tempfiledrop.webserver.exception.ApiException
import com.tempfiledrop.webserver.exception.ErrorCode
import com.tempfiledrop.webserver.service.storagesvcclient.StorageSvcClientImpl
import com.tempfiledrop.webserver.service.userinfo.UserInfoServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/uploads")
class UserUploadRecordController(
        private val serverProperties: ServerProperties,
        private val userInfoService: UserInfoServiceImpl,
        private val userUploadRecordService: UserUploadRecordServiceImpl,
        private val storageSvcClient: StorageSvcClientImpl
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserUploadRecordController::class.java)
    }

    @GetMapping("/list/{username}")
    fun getUserUploadRecords(@PathVariable("username") username: String): ResponseEntity<List<UserUploadRecord>> {
        logger.info("Retrieving records for $username")
        val user = userInfoService.getUserInfoByUsername(username) ?: throw ApiException("User not found!", ErrorCode.USER_NOT_FOUND, HttpStatus.BAD_REQUEST)
        val records = userUploadRecordService.listUserUploadRecord(user.username)
        logger.info("${records.size} records found")
        return ResponseEntity(records, HttpStatus.OK)
    }

    @DeleteMapping("/delete/{username}/{recordId}")
    fun deleteUserUploadRecords(@PathVariable("username") username: String, @PathVariable("recordId") recordID: String): ResponseEntity<Void> {
        logger.info("Deleting record - $recordID")
        val user = userInfoService.getUserInfoByUsername(username) ?: throw ApiException("User not found!", ErrorCode.USER_NOT_FOUND, HttpStatus.BAD_REQUEST)
        val record = userUploadRecordService.getUserUploadRecordById(recordID) ?: throw ApiException("Record not found!", ErrorCode.NO_MATCHING_RECORD, HttpStatus.BAD_REQUEST)
        userUploadRecordService.deleteUserUploadRecord(record.id!!)

        // delete record from storage service
        storageSvcClient.deleteFilesInFolder(serverProperties.bucketName, record.storageId)

        return ResponseEntity.noContent().build()
    }
}