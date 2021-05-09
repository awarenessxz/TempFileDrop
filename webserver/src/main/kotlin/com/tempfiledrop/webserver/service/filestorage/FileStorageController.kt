package com.tempfiledrop.webserver.service.filestorage

import com.tempfiledrop.webserver.config.ServerProperties
import com.tempfiledrop.webserver.exception.ApiException
import com.tempfiledrop.webserver.exception.ErrorCode
import com.tempfiledrop.webserver.service.storagesvcclient.StorageSvcClientImpl
import com.tempfiledrop.webserver.service.storagesvcclient.StorageUploadRequest
import com.tempfiledrop.webserver.service.uploadedfiles.UploadedFiles
import com.tempfiledrop.webserver.service.uploadedfiles.UploadedFilesServiceImpl
import com.tempfiledrop.webserver.util.StorageUtils
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/files")
class FileStorageController(
        private val serverProperties: ServerProperties,
        private val uploadedFilesRecordService: UploadedFilesServiceImpl,
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
            val storageRequest = StorageUploadRequest(serverProperties.bucketName, storagePath)
            val storageSvcResponse = storageSvcClient.uploadToStorageSvc(files, storageRequest)
            val fileStorageResponse = storageSvcResponse.body
            if (!storageSvcResponse.statusCode.is2xxSuccessful || fileStorageResponse == null) {
                throw ApiException("Uploading Failed!", ErrorCode.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR)
            }

            // store the storage information into database
            logger.info("Adding user upload record to database -- ${fileStorageResponse.storageId}")
            val expiryDatetime = StorageUtils.processExpiryPeriod(metadata.expiryPeriod)
            val filenames = files.joinToString(",") { it.originalFilename.toString() }
            uploadedFilesRecordService.addUploadedFilesRecord(UploadedFiles(null, storagePath, filenames, metadata.maxDownloads, expiryDatetime, fileStorageResponse.storageId, fileStorageResponse.downloadLink))

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
        uploadedFilesRecordService.reduceDownloadCountOfUploadedFiles(storageId)
        return storageSvcClient.downloadFromStorageSvc(serverProperties.bucketName, storageId)
    }
}