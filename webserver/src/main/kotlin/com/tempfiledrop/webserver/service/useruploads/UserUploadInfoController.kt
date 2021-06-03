package com.tempfiledrop.webserver.service.useruploads

import com.tempfiledrop.webserver.exception.ApiException
import com.tempfiledrop.webserver.exception.ErrorCode
import com.tempfiledrop.webserver.model.StorageInfoBulkRequest
import com.tempfiledrop.webserver.model.StorageInfoBulkResponse
import com.tempfiledrop.webserver.model.StorageInfoResponse
import com.tempfiledrop.webserver.service.userinfo.UserInfoService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping("/api/users-upload-info")
class UserUploadInfoController(
        @Value("\${tempfiledrop.bucket-name}") private val tempfiledropBucket: String,
        @Value("\${tempfiledrop.storagesvc-url}") private val storageServiceUrl: String,
        private val userInfoService: UserInfoService,
        private val uploadedFilesRecordService: UserUploadInfoService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserUploadInfoController::class.java)
    }

    @GetMapping("/{username}")
    fun getUploadedFilesRecords(@PathVariable("username") username: String): ResponseEntity<List<UserUploadInfoResponse>> {
        logger.info("Retrieving records for $username")
        val user = userInfoService.getUserInfoByUsername(username)
                ?: throw ApiException("User not found!", ErrorCode.USER_NOT_FOUND, HttpStatus.BAD_REQUEST)
        val records = uploadedFilesRecordService.getUploadedFilesRecordsForUser(user.username)

        logger.info("${records.size} records found")
        val results = ArrayList<UserUploadInfoResponse>()
        if (records.isNotEmpty()) {
            // get information from storage service
            val storageIds = records.map { it.storageId }
            val restTemplate = RestTemplate()
            val request = StorageInfoBulkRequest(tempfiledropBucket, storageIds)
            val response = restTemplate.postForEntity("$storageServiceUrl/storagesvc/storageinfo/bulk", request, StorageInfoBulkResponse::class.java)
            val storageInfoList = response.body?.storageInfoList

            if (!response.statusCode.is2xxSuccessful || storageInfoList === null) {
                throw ApiException("Fail to retrieved storage info...", ErrorCode.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR)
            }

            // process records
            val storageInfoMap = storageInfoList.map { it.storageId to it }.toMap()
            records.forEach {
                if (storageInfoMap.containsKey(it.storageId)) {
                    // get details
                    val storageInfo = storageInfoMap[it.storageId]
                            ?: throw ApiException("Fail to retrieved storage info...", ErrorCode.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR)
                    results.add(UserUploadInfoResponse(it.id, storageInfo))
                } else {
                    // file is not available anymore, delete the record
                    uploadedFilesRecordService.deleteUploadedFilesRecordById(it.id!!)
                }
            }
        }
        return ResponseEntity(results, HttpStatus.OK)
    }
}