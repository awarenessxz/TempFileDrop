package com.tempfiledrop.webserver.service.filestorage

import com.tempfiledrop.webserver.config.ServerProperties
import com.tempfiledrop.webserver.exception.ApiException
import com.tempfiledrop.webserver.exception.ErrorCode
import com.tempfiledrop.webserver.service.storagesvcclient.StorageInfoResponse
import com.tempfiledrop.webserver.service.storagesvcclient.StorageSvcClientImpl
import com.tempfiledrop.webserver.service.storagesvcclient.StorageUploadRequest
import com.tempfiledrop.webserver.service.useruploads.UserUploadInfo
import com.tempfiledrop.webserver.service.useruploads.UserUploadInfoController
import com.tempfiledrop.webserver.service.useruploads.UserUploadInfoServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.multipart.MultipartFile
import java.lang.Exception
import java.time.ZonedDateTime

@RestController
@RequestMapping("/api/files")
class FileStorageController(
        private val serverProperties: ServerProperties,
        private val uploadedFilesRecordService: UserUploadInfoServiceImpl,
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
        logger.info("Received Request to store files in ${serverProperties.bucketName}/$storagePath")

        try {
            // Forward to Storage Service to store file
            val storageRequest = StorageUploadRequest(serverProperties.bucketName, storagePath, metadata.maxDownloads, metadata.expiryPeriod)
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
    fun downloadFiles(@PathVariable("storageId") storageId: String): ResponseEntity<Resource> {
        logger.info("Receiving request to download files from $storageId")
        return storageSvcClient.downloadFromStorageSvc(serverProperties.bucketName, storageId)
    }


    @GetMapping("/download-info/{storageId}")
    fun getStorageInformationForDownload(@PathVariable("storageId") storageId: String): ResponseEntity<StorageInfoResponse> {
        logger.info("Receiving request to get download information for $storageId")

        // verify if files exists on server
        val storageSvcResponse = storageSvcClient.getStorageInfoByStorageId(serverProperties.bucketName, storageId)
        val storageInfoResponse = storageSvcResponse.body
        if (!storageSvcResponse.statusCode.is2xxSuccessful || storageInfoResponse === null) {
            logger.info("WHAT HAPPENED --> $storageSvcResponse vs $storageInfoResponse")
            throw ApiException("Fail to retrieve download information.", ErrorCode.NO_MATCHING_RECORD, HttpStatus.BAD_REQUEST)
        }

        // one more step to delete the records!! (Try retrieving) If cannot, then delete) above )
        val response = StorageInfoResponse(storageInfoResponse.storageId, storageInfoResponse.downloadLink, storageInfoResponse.filenames, storageInfoResponse.numOfDownloadsLeft, storageInfoResponse.expiryDatetime)
        return ResponseEntity(response, HttpStatus.OK)
    }
}