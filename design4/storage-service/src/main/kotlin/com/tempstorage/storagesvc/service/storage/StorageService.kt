package com.tempstorage.storagesvc.service.storage

import com.tempstorage.storagesvc.config.StorageSvcProperties
import com.tempstorage.storagesvc.controller.storage.StorageS3UploadUrlParams
import com.tempstorage.storagesvc.controller.storage.StorageUploadMetadata
import com.tempstorage.storagesvc.controller.storage.StorageUploadResponse
import com.tempstorage.storagesvc.controller.storage.StorageS3UploadUrlResponse
import com.tempstorage.storagesvc.exception.ApiException
import com.tempstorage.storagesvc.exception.ErrorCode
import com.tempstorage.storagesvc.service.storageinfo.StorageInfo
import com.tempstorage.storagesvc.service.storageinfo.StorageInfoService
import com.tempstorage.storagesvc.service.storageinfo.StorageStatus
import com.tempstorage.storagesvc.util.StorageUtils
import io.minio.http.Method
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import javax.servlet.http.HttpServletRequest

abstract class StorageService {
    companion object {
        private val logger = LoggerFactory.getLogger(StorageService::class.java)
    }

    abstract fun initStorage()
    abstract fun getS3PresignedUrl(bucket: String, objectName: String, method: Method): String?
    abstract fun getS3PostUploadUrl(bucket: String, objectName: String): Map<String, String>?
    abstract fun uploadFilesViaStream(request: HttpServletRequest, isAnonymous: Boolean): List<StorageInfo>
//    abstract fun deleteFile(storageInfo: StorageInfo, eventData: String? = "")
//    abstract fun downloadFile(storageInfo: StorageInfo, response: HttpServletResponse, eventData: String? = "")
//    abstract fun downloadFilesAsZip(storageInfo: StorageInfo, storageFiles: List<StorageFile>, response: HttpServletResponse, eventData: String? = "")
//    abstract fun getAllFileSizeInBucket(bucket: String, storageInfoList: List<StorageInfo>): List<StorageInfo>

    private lateinit var storageInfoService: StorageInfoService
    private lateinit var storageServiceProperties: StorageSvcProperties

    @Autowired
    fun setStorageInfoService(storageInfoService: StorageInfoService) {
        this.storageInfoService = storageInfoService
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
//
//    fun deleteFromBucket(storageId: String, objectName: String, eventData: String) {
//        val storageInfo = getStorageInfoFromDatabase(storageId, objectName)
//        validateStorageInfo(storageInfo)
//        deleteFile(storageInfo, eventData)
//    }

    /***************************************************************************************************************************************************************
     * Download Functions
     ***************************************************************************************************************************************************************/

//    fun downloadFileFromBucket(storageId: String, objectName: String, response: HttpServletResponse, eventData: String) {
//        val storageInfo = getStorageInfoFromDatabase(storageId, objectName)
//        validateStorageInfo(storageInfo)
//        // download
//        response.contentType = storageInfo.fileContentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE
//        // response.setContentLengthLong(storageFile.fileLength)
//        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${storageInfo.originalFilename}\"")
//        downloadFile(storageInfo, response, eventData) // IMPORTANT: ORDER MATTERS (MUST BE AFTER SETTING HEADER)
//    }

    /***************************************************************************************************************************************************************
     * Upload Functions
     ***************************************************************************************************************************************************************/

    // Generate Http Endpoints / S3 Presigned Urls (Put & Post) and store Metadata into Database (for publishing event after upload completes)
    fun generateS3UploadUrl(params: StorageS3UploadUrlParams): StorageS3UploadUrlResponse {
        // Generate Endpoints
        val s3Endpoint = listOf(storageServiceProperties.objectStorage.minioEndpoint, params.bucket).filter { it.isNotEmpty() }.joinToString("/")
        val s3PutEndpoints = mutableMapOf<String, String>()
        val s3PostEndpoints = mutableMapOf<String, Map<String, String>>()
        params.storageObjects.forEach { objectName ->
            getS3PresignedUrl(params.bucket, objectName, Method.PUT)?.let {
                s3PutEndpoints[objectName] = it
            }
            getS3PostUploadUrl(params.bucket, objectName)?.let {
                s3PostEndpoints[objectName] = it
            }
        }

        // Store Metadata as Storage Info (Pending) in Database
        params.storageObjects.map {
            StorageInfo(
                    params.bucket,
                    it,
                    "",
                    -1,
                    params.maxDownloads!!,
                    StorageUtils.processExpiryPeriod(params.expiryPeriod!!),
                    params.allowAnonymousDownload!!,
                    StorageStatus.PENDING,
                    params.customEventData
            )
        }.forEach { storageInfoService.addStorageInfo(it) }

        // Return Response
        return StorageS3UploadUrlResponse(s3PutEndpoints, s3PostEndpoints, s3Endpoint)
    }

    // upload files via apache commons fileupload streaming api
    fun uploadViaStreamToBucket(request: HttpServletRequest, isAnonymous: Boolean = false): StorageUploadResponse {
        val isMultipart = ServletFileUpload.isMultipartContent(request)
        if (!isMultipart) {
            throw ApiException("Invalid Multipart Request", ErrorCode.CLIENT_ERROR, HttpStatus.BAD_REQUEST)
        }
        val uploadedFiles = uploadFilesViaStream(request, isAnonymous) // upload files
        val storagePathList = uploadedFiles.map { it.storageFullPath }
        return StorageUploadResponse("Files uploaded successfully", storagePathList)
    }

    /*******************************************************************************************************
     *                                                                                                     *
     * INTERNAL FUNCTION                                                                                   *
     *                                                                                                     *
     *******************************************************************************************************/

//    private fun getStorageInfoFromDatabase(storageId: String, objectName: String): StorageInfo {
//        return if (storageId.isNotEmpty()) {
//            storageInfoService.getStorageInfoById(storageId) ?: throw ApiException("Files not found!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
//        } else if (objectName.isNotEmpty()) {
//            storageInfoService.getStorageInfoByPath(objectName) ?: throw ApiException("Files not found!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
//        } else {
//            throw ApiException("Please provide either storageId or objectName...", ErrorCode.CLIENT_ERROR, HttpStatus.BAD_REQUEST)
//        }
//    }
//
//    private fun validateStorageInfo(storageInfo: StorageInfo, throwError: Boolean? = true): Boolean {
//        var isValid = true
//        if (storageInfo.numOfDownloadsLeft <= 0 || storageInfo.expiryDatetime.isBefore(ZonedDateTime.now())) {
//            // storage expired. Delete the records and objects
//            deleteFile(storageInfo)
//            storageInfoService.deleteStorageInfoById(storageInfo.id)
//            isValid = false
//        }
//        if (throwError!! && !isValid) {
//            throw ApiException("File have expired!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
//        }
//        return isValid
//    }
}