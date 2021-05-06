package com.tempfiledrop.webserver.service.filestorage

import com.tempfiledrop.webserver.config.ServerProperties
import com.tempfiledrop.webserver.exception.ApiException
import com.tempfiledrop.webserver.exception.ErrorCode
import com.tempfiledrop.webserver.service.storagesvcclient.StorageRequest
import com.tempfiledrop.webserver.service.storagesvcclient.StorageSvcClientImpl
import com.tempfiledrop.webserver.service.useruploads.UserUploadRecord
import com.tempfiledrop.webserver.service.useruploads.UserUploadRecordServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/files")
class FileStorageController(
        private val serverProperties: ServerProperties,
        private val userUploadRecordService: UserUploadRecordServiceImpl,
        private val storageSvcClient: StorageSvcClientImpl
) {
    companion object {
        private val logger = LoggerFactory.getLogger(FileStorageController::class.java)
        private const val ANONYMOUS_FOLDER = "anonymous"
    }

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
            @RequestPart("files", required = true) files: List<MultipartFile>,
            @RequestPart("metadata", required = true) metadata: FileUploadInfoRequest
    ): ResponseEntity<FileUploadInfoResponse> {
        val filesStored = files.joinToString(", ") { it.originalFilename.toString() }
        val storagePath = if (metadata.username.trim().isEmpty()) ANONYMOUS_FOLDER else metadata.username
        logger.info("Received Request to store files in ${serverProperties.bucketName}/$storagePath <-- $filesStored")

        try {
            // Forward to Storage Service to store file
            val storageRequest = StorageRequest(serverProperties.bucketName, storagePath, metadata.maxDownloads, metadata.expiryPeriod)
            val storageSvcResponse = storageSvcClient.uploadToStorageSvc(files, storageRequest)

            // store the storage information into database
            val fileStorageResponse = storageSvcResponse.body
            if (!storageSvcResponse.statusCode.is2xxSuccessful || fileStorageResponse == null) {
                throw ApiException("Uploading Failed!", ErrorCode.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR)
            } else {
                if (storagePath != ANONYMOUS_FOLDER) {
                    // to change the download url
                    logger.info("Adding user upload record to database -- ${fileStorageResponse.storageId}")
                    userUploadRecordService.addUserUploadRecord(UserUploadRecord(null, storagePath, filesStored, fileStorageResponse.storageId, ""))
                }
            }

            // process response & return results
            return if (storageSvcResponse.statusCode.is2xxSuccessful) {
                val message = "Uploaded Successfully: $filesStored"
                logger.info("Upload is successful!")
                ResponseEntity(FileUploadInfoResponse(message, fileStorageResponse.storageId, ""), HttpStatus.OK)
            } else {
                logger.info("Upload Failed!")
                val message = "Could not upload the files! - $filesStored"
                ResponseEntity(FileUploadInfoResponse(message, fileStorageResponse.storageId, ""), HttpStatus.INTERNAL_SERVER_ERROR)
            }
        } catch (e: HttpStatusCodeException) {
            logger.info("ERROR -- ${e.message}")
            throw ApiException("Uploading Failed!", ErrorCode.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}