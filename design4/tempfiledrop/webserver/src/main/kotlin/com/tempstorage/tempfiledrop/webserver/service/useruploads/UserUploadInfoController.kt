package com.tempstorage.tempfiledrop.webserver.service.useruploads

import com.tempstorage.tempfiledrop.webserver.exception.ApiException
import com.tempstorage.tempfiledrop.webserver.exception.ErrorCode
import com.tempstorage.tempfiledrop.webserver.model.StorageMetadata
import com.tempstorage.tempfiledrop.webserver.model.StorageMetadataResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/tempfiledrop/users-upload-info")
class UserUploadInfoController(
        @Value("\${tempfiledrop.storagesvc-url}") private val storageServiceUrl: String,
        private val uploadedFilesRecordService: UserUploadInfoService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserUploadInfoController::class.java)
    }

    @GetMapping("/{username}")
    fun getUploadedFilesRecords(@PathVariable("username") username: String): ResponseEntity<List<StorageMetadata>> {
        logger.info("Retrieving records for $username")
        val records = uploadedFilesRecordService.getUploadedFilesRecordsForUser(username)

        logger.info("${records.size} records found")
        val results = ArrayList<StorageMetadata>()
        if (records.isNotEmpty()) {
            // get information from storage service
            val storageObjects = records.map { it.objectName }
            val builder = UriComponentsBuilder.fromHttpUrl("$storageServiceUrl/api/storagesvc/metadata")
                    .queryParam("storageObjects", storageObjects)
            val restTemplate = RestTemplate()
            val response = restTemplate.getForEntity(builder.toUriString(), StorageMetadataResponse::class.java)
            val storageInfoList = response.body?.storageMetadataList

            if (!response.statusCode.is2xxSuccessful || storageInfoList === null) {
                throw ApiException("Fail to retrieved storage info...", ErrorCode.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR)
            }

            // process records
            records.forEach {
                if (storageInfoList.containsKey(it.objectName)) {
                    // get details
                    val storageInfo = storageInfoList[it.objectName] ?: throw ApiException("Fail to retrieved storage info...", ErrorCode.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR)
                    results.add(storageInfo)
                } else {
                    // file is not available anymore, delete the record
                    uploadedFilesRecordService.deleteUploadedFilesRecordById(it.id.toString())
                }
            }
        }
        return ResponseEntity(results, HttpStatus.OK)
    }
}