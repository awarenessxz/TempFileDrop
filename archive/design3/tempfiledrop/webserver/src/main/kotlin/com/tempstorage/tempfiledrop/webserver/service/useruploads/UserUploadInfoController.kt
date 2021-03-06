package com.tempstorage.tempfiledrop.webserver.service.useruploads

import com.tempstorage.tempfiledrop.webserver.exception.ApiException
import com.tempstorage.tempfiledrop.webserver.exception.ErrorCode
import com.tempstorage.tempfiledrop.webserver.model.StorageInfoBulkRequest
import com.tempstorage.tempfiledrop.webserver.model.StorageInfoBulkResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import java.security.Principal
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/users-upload-info")
class UserUploadInfoController(
        @Value("\${tempfiledrop.bucket-name}") private val tempfiledropBucket: String,
        @Value("\${tempfiledrop.storagesvc-url}") private val storageServiceUrl: String,
        private val uploadedFilesRecordService: UserUploadInfoService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserUploadInfoController::class.java)
    }

    @GetMapping("/print")
    fun printUser(request: HttpServletRequest, principal: Principal, authentication: Authentication): ResponseEntity<Void> {
        logger.info("Principal = $principal")
        logger.info("Authentication = ${authentication.principal}")
        logger.info("Request = ${request.headerNames}")
        logger.info("Security Context = ${SecurityContextHolder.getContext().authentication}")
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{username}")
    fun getUploadedFilesRecords(
            @PathVariable("username") username: String,
            @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<List<UserUploadInfoResponse>> {
        logger.info("Retrieving records for $username")
        val records = uploadedFilesRecordService.getUploadedFilesRecordsForUser(username)

        logger.info("${records.size} records found")
        val results = ArrayList<UserUploadInfoResponse>()
        if (records.isNotEmpty()) {
            // get information from storage service
            val storageIds = records.map { it.storageId }
            val request = StorageInfoBulkRequest(tempfiledropBucket, storageIds)
            val httpHeader = HttpHeaders()
            httpHeader.set("Authorization", authHeader)
            val entity = HttpEntity(request, httpHeader)
            val restTemplate = RestTemplate()
            val response = restTemplate.postForEntity("$storageServiceUrl/api/storagesvc/storageinfo/bulk", entity, StorageInfoBulkResponse::class.java)
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