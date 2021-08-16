package com.tempstorage.storagesvc.service.storage

import com.tempstorage.storagesvc.config.StorageSvcProperties
import com.tempstorage.storagesvc.controller.storage.StorageS3PresignedUrlParams
import com.tempstorage.storagesvc.controller.storage.StorageUploadResponse
import com.tempstorage.storagesvc.controller.storage.StorageS3PresignedUrlResponse
import com.tempstorage.storagesvc.exception.ApiException
import com.tempstorage.storagesvc.exception.ErrorCode
import com.tempstorage.storagesvc.service.notification.NotificationService
import com.tempstorage.storagesvc.service.metadata.StorageMetadata
import com.tempstorage.storagesvc.service.metadata.StorageMetadataService
import com.tempstorage.storagesvc.util.StorageUtils
import io.minio.http.Method
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.time.ZonedDateTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

abstract class StorageService {
    companion object {
        private val logger = LoggerFactory.getLogger(StorageService::class.java)
    }

    abstract fun initStorage()
    abstract fun getS3PresignedUrl(metadata: StorageMetadata, method: Method): String?
    abstract fun getS3PostUploadUrl(metadata: StorageMetadata): Map<String, String>?
    abstract fun uploadFilesViaStream(request: HttpServletRequest, isAnonymous: Boolean): List<StorageMetadata>
    abstract fun deleteFile(storageMetadata: StorageMetadata)
    abstract fun downloadFile(storageMetadata: StorageMetadata, response: HttpServletResponse)
    abstract fun downloadFilesAsZip(storageMetadataList: List<StorageMetadata>, response: HttpServletResponse)
//    abstract fun getAllFileSizeInBucket(bucket: String, storageInfoList: List<StorageInfo>): List<StorageInfo>

    private lateinit var storageMetadataService: StorageMetadataService
    private lateinit var notificationService: NotificationService
    private lateinit var storageServiceProperties: StorageSvcProperties

    @Autowired
    fun setStorageInfoService(storageMetadataService: StorageMetadataService) {
        this.storageMetadataService = storageMetadataService
    }

    @Autowired
    fun setNotificationService(notificationService: NotificationService) {
        this.notificationService = notificationService
    }

    @Autowired
    fun setStorageServiceProperties(storageServiceProperties: StorageSvcProperties) {
        this.storageServiceProperties = storageServiceProperties
    }

//    fun getAllBuckets(): List<String> {
//        return storageInfoService.getBuckets()
//    }
//
//    fun getAllStorageInfoFromBucket(bucket: String): List<StorageInfo> {
//        val results = storageInfoService.getAllStorageInfoInBucket(bucket)
//        return results.filter { validateStorageInfo(it, false) }
//    }
//
//    fun getStorageInfoFromBucket(storageId: String, objectName: String, throwError: Boolean? = true): StorageInfo {
//        val storageInfo = getStorageInfoFromDatabase(storageId, objectName)
//        validateStorageInfo(storageInfo, throwError)
//        return storageInfo
//    }

//    // convert list of all storage in bucket into folder like structure
//    fun listFilesAndFoldersInBucket(bucket: String): FileSystemNode {
//        val storageInfoList = getAllStorageInfoFromBucket(bucket)
//        val fileSystemNodes = getAllFileSizeInBucket(bucket, storageInfoList).map {
//            FileSystemNode(true, it.originalFilename, it.objectName, it.bucket, it.id, it.fileLength.toInt(), it.numOfDownloadsLeft, it.expiryDatetime)
//        }
//        return StorageUtils.buildFolderTreeStructure(bucket, fileSystemNodes)
//    }


    /***************************************************************************************************************************************************************
     * Delete Functions
     ***************************************************************************************************************************************************************/

    fun deleteFilesInBucket(storageObjects: List<String>): Map<String, String> {
        // get and validate object
        val results: MutableMap<String, String> = HashMap()
        val storageMetadataList = storageObjects.map { objectName -> getStorageMetadataFromDatabase(objectName) }
        storageMetadataList.forEach { metadata ->
            if (validateStorageMetadata(metadata, false)) {
                deleteFile(metadata)
                results[metadata.objectName] = "File Pending Deletion"
            } else {
                results[metadata.objectName] = "File Not Found"
            }
        }
        return results
    }

    /***************************************************************************************************************************************************************
     * Presigned Url Functions
     ***************************************************************************************************************************************************************/

    fun generateS3PresignedUrl(params: StorageS3PresignedUrlParams, method: Method): StorageS3PresignedUrlResponse {
        // Generate Endpoints
        val s3Endpoint = listOf(storageServiceProperties.objectStorage.minioEndpoint, params.bucket).filter { it.isNotEmpty() }.joinToString("/")
        val s3PutEndpoints = mutableMapOf<String, String>()
        val s3PostEndpoints = mutableMapOf<String, Map<String, String>>()
        params.storageObjects.forEach { objectName ->
            val metadata = StorageMetadata(params.bucket, objectName, "", -1, StorageUtils.processMaxDownloadCount(params.maxDownloads), StorageUtils.processExpiryPeriod(params.expiryPeriod ?: 1), false)
            getS3PresignedUrl(metadata, method)?.let {
                s3PutEndpoints[objectName] = it
            }
            if (method == Method.PUT) {
                getS3PostUploadUrl(metadata)?.let {
                    s3PostEndpoints[objectName] = it
                }
            }
        }

        // Return Response
        return StorageS3PresignedUrlResponse(s3PutEndpoints, s3PostEndpoints, s3Endpoint)
    }

    /***************************************************************************************************************************************************************
     * Download Functions
     ***************************************************************************************************************************************************************/

    fun downloadFilesFromBucket(storageObjects: List<String>, response: HttpServletResponse) {
        // get and validate object
        val storageMetadataList = storageObjects.map { objectName -> getStorageMetadataFromDatabase(objectName) }
        storageMetadataList.forEach { metadata -> validateStorageMetadata(metadata) }

        // download files
        if (storageMetadataList.size > 1) {
            // multiple file (download as zip)
            logger.info("Zip File Download...")
            response.status = HttpServletResponse.SC_OK
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"storage.zip\"")
            downloadFilesAsZip(storageMetadataList, response) // IMPORTANT: ORDER MATTERS (MUST BE AFTER SETTING HEADER)
        } else {
            // single file download
            logger.info("Single File Download...")
            val storageInfo = storageMetadataList[0]
            response.contentType = storageInfo.fileContentType
            // response.setContentLengthLong(storageFile.fileLength)
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${storageInfo.getOriginalFilename()}\"")
            downloadFile(storageInfo, response) // IMPORTANT: ORDER MATTERS (MUST BE AFTER SETTING HEADER)
        }
    }

    /***************************************************************************************************************************************************************
     * Upload Functions
     ***************************************************************************************************************************************************************/

    // upload files via apache commons fileupload streaming api
    fun uploadViaStreamToBucket(request: HttpServletRequest, isAnonymous: Boolean = false): StorageUploadResponse {
        val isMultipart = ServletFileUpload.isMultipartContent(request)
        if (!isMultipart) {
            throw ApiException("Invalid Multipart Request", ErrorCode.CLIENT_ERROR, HttpStatus.BAD_REQUEST)
        }
        val uploadedFiles = uploadFilesViaStream(request, isAnonymous) // upload files
        val storagePathList = uploadedFiles.map { it.getStorageFullPath() }
        return StorageUploadResponse("Files uploaded successfully", storagePathList)
    }

    /*******************************************************************************************************
     *                                                                                                     *
     * INTERNAL FUNCTION                                                                                   *
     *                                                                                                     *
     *******************************************************************************************************/

    private fun getStorageMetadataFromDatabase(objectName: String): StorageMetadata {
        return if (objectName.isNotEmpty()) {
            storageMetadataService.getStorageMetadataByObjectName(objectName) ?: throw ApiException("Files not found!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
        } else {
            throw ApiException("Please provide either storageId or objectName...", ErrorCode.CLIENT_ERROR, HttpStatus.BAD_REQUEST)
        }
    }

    private fun validateStorageMetadata(storageMetadata: StorageMetadata, throwError: Boolean? = true): Boolean {
        var isValid = true
        if ((storageMetadata.numOfDownloadsLeft != -999 && storageMetadata.numOfDownloadsLeft <= 0)
                || (storageMetadata.expiryDatetime != null && storageMetadata.expiryDatetime.isBefore(ZonedDateTime.now()))) {
            deleteFile(storageMetadata) // storage expired. Delete the object
            isValid = false
        }
        if (throwError!! && !isValid) {
            throw ApiException("File have expired!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
        }
        return isValid
    }
}