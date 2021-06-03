package com.tempfiledrop.webserver.service.filestorage

import com.fasterxml.jackson.databind.ObjectMapper
import com.tempfiledrop.webserver.exception.ApiException
import com.tempfiledrop.webserver.exception.ErrorCode
import com.tempfiledrop.webserver.exception.ErrorResponse
import com.tempfiledrop.webserver.service.storagesvcclient.StorageInfoResponse
import com.tempfiledrop.webserver.service.storagesvcclient.StorageSvcClientImpl
import com.tempfiledrop.webserver.service.storagesvcclient.StorageUploadRequest
import com.tempfiledrop.webserver.service.useruploads.UserUploadInfo
import com.tempfiledrop.webserver.service.useruploads.UserUploadInfoService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api/files")
class FileStorageController(
        @Value("\${tempfiledrop.webserver.bucket-name}") private val tempfiledropBucket: String,
        private val uploadedFilesRecordService: UserUploadInfoService,
        private val storageSvcClient: StorageSvcClientImpl
) {
    companion object {
        private val logger = LoggerFactory.getLogger(FileStorageController::class.java)
        private const val ANONYMOUS_FOLDER = "anonymous"
    }

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFiles(
            @RequestPart("files", required = true) files: List<MultipartFile>,
            @RequestPart("metadata", required = true) metadata: FileUploadInfoRequest
    ): ResponseEntity<FileUploadInfoResponse> {
        val storagePath = if (metadata.username.trim().isEmpty()) ANONYMOUS_FOLDER else metadata.username
        logger.info("Received Request to store ${files.size} files in $tempfiledropBucket/$storagePath")

        try {
            // Forward to Storage Service to store file
            val storageRequest = StorageUploadRequest(tempfiledropBucket, storagePath, metadata.maxDownloads, metadata.expiryPeriod)
            val storageSvcResponse = storageSvcClient.uploadToStorageSvc(files, storageRequest)
            val fileStorageResponse = storageSvcResponse.body
            if (!storageSvcResponse.statusCode.is2xxSuccessful || fileStorageResponse == null) {
                throw ApiException("Uploading Failed!", ErrorCode.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR)
            }

            // store the storage information into database
            logger.info("Adding user upload record to database -- ${fileStorageResponse.storageId}")
            uploadedFilesRecordService.addUploadedFilesRecord(UserUploadInfo(null, storagePath, fileStorageResponse.storageId))

            // process response & return results
            return if (storageSvcResponse.statusCode.is2xxSuccessful) {
                logger.info("Upload is successful!")
                ResponseEntity(FileUploadInfoResponse("Upload Successful", fileStorageResponse.storageId), HttpStatus.OK)
            } else {
                logger.info("Upload Failed!")
                throw ApiException("Could not upload the files!", ErrorCode.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR)
            }
        } catch (e: HttpStatusCodeException) {
            logger.info("ERROR -- ${e.message}")
            throw ApiException("Uploading Failed!", ErrorCode.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/download/{storageId}")
    fun downloadFiles(@PathVariable("storageId") storageId: String, response: HttpServletResponse) {
        logger.info("Receiving request to download files from $storageId")
        storageSvcClient.downloadFromStorageSvc(tempfiledropBucket, storageId, response)
    }

    @GetMapping("/download-info/{storageId}")
    fun getStorageInformationForDownload(@PathVariable("storageId") storageId: String): ResponseEntity<StorageInfoResponse> {
        logger.info("Receiving request to get download information for $storageId")

        // verify if files exists on server
        try {
            val storageSvcResponse = storageSvcClient.getStorageInfoByStorageId(tempfiledropBucket, storageId)
            val storageInfoResponse = storageSvcResponse.body
            if (!storageSvcResponse.statusCode.is2xxSuccessful || storageInfoResponse === null) {
                throw ApiException("Fail to retrieve download information for $storageId", ErrorCode.NO_MATCHING_RECORD, HttpStatus.BAD_REQUEST)
            }
            val response = StorageInfoResponse(storageInfoResponse.storageId, storageInfoResponse.downloadLink, storageInfoResponse.filenames, storageInfoResponse.numOfDownloadsLeft, storageInfoResponse.expiryDatetime)
            return ResponseEntity(response, HttpStatus.OK)
        } catch (e: HttpStatusCodeException) {
            logger.error(e.responseBodyAsString)
            val objectMapper = ObjectMapper()
            val errorResponse = objectMapper.readValue(e.responseBodyAsString, ErrorResponse::class.java)
            if (errorResponse.errorCode === ErrorCode.FILE_NOT_FOUND) {
                // delete messages
                logger.info("Deleting User Uploaded Record for $storageId....")
                uploadedFilesRecordService.deleteUploadedFilesRecordByStorageId(storageId)
            }
            throw ApiException("Fail to retrieve download information for $storageId", ErrorCode.NO_MATCHING_RECORD, HttpStatus.BAD_REQUEST)
        }
    }
}