package com.tempstorage.storagesvc.service.storage

import com.tempstorage.storagesvc.exception.ApiException
import com.tempstorage.storagesvc.exception.ErrorCode
import com.tempstorage.storagesvc.service.storageinfo.StorageInfo
import com.tempstorage.storagesvc.service.storageinfo.StorageInfoService
import com.tempstorage.storagesvc.util.StorageUtils
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.ZonedDateTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

abstract class StorageService {
    companion object {
        private val logger = LoggerFactory.getLogger(StorageService::class.java)
    }

    abstract fun initStorage()
    abstract fun uploadFilesViaStream(request: HttpServletRequest, storageId: String, isAnonymous: Boolean)
    abstract fun deleteFile(storageInfo: StorageInfo, eventData: String? = "")
    abstract fun downloadFile(storageInfo: StorageInfo, response: HttpServletResponse, eventData: String? = "")
//    abstract fun downloadFilesAsZip(storageInfo: StorageInfo, storageFiles: List<StorageFile>, response: HttpServletResponse, eventData: String? = "")
//    abstract fun getAllFileSizeInBucket(bucket: String, storageFiles: List<StorageFile>): List<StorageFile>

    private lateinit var storageInfoService: StorageInfoService

    @Autowired
    fun setStorageInfoService(storageInfoService: StorageInfoService) {
        this.storageInfoService = storageInfoService
    }

//    fun getAllBuckets(): List<String> {
//        return storageInfoService.getBuckets()
//    }
//
//    fun getAllStorageInfo(bucket: String? = null): List<StorageInfo> {
//        var allStorageInfoList = storageInfoService.getAllStorageInfo()
//        if (bucket != null) {
//            allStorageInfoList = allStorageInfoList.filter { it.bucket == bucket }
//        }
//        return allStorageInfoList.filter { checkIfStorageFileIsAvailable(it) }
//    }
//
//    // convert list of all storage in bucket into folder like structure
//    fun listFilesAndFoldersInBucket(bucket: String): FileSystemNode {
//        val storageInfoList = getAllStorageInfo(bucket)
//        val storageIds = storageInfoList.map { it.id.toString() }
//        val storageFiles = storageFileService.getStorageFilesInfoByStorageIdBulk(storageIds)
//        val storageFilesWithFileSize = getAllFileSizeInBucket(bucket, storageFiles)
//
//        val filesystemNodes = storageInfoList.map { storageInfo ->
//            val storageSize = storageFilesWithFileSize.filter { it.storageId == storageInfo.id.toString() }.sumBy { it.fileLength.toInt() }
//            FileSystemNode(true, storageInfo.filenames, "${storageInfo.storagePath}/${storageInfo.filenames}", storageInfo.bucket, storageInfo.id.toString(), storageSize, storageInfo.numOfDownloadsLeft, storageInfo.expiryDatetime)
//        }
//        return StorageUtils.buildFolderTreeStructure(bucket, filesystemNodes)
//    }
//
//    fun getStorageInfoFromBucket(bucket: String, storageId: String): StorageInfo {
//        return getAndValidateStorageInfo(storageId, bucket)
//    }
//
//    fun getMultipleStorageInfoFromBucket(bucket: String, storageIdList: List<String>): List<StorageInfo> {
//        val storageInfoList = storageInfoService.getBulkStorageInfoById(storageIdList)
//        if (storageInfoList.any { it.bucket != bucket}) {
//            throw ApiException("Storage Id not available in Bucket!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
//        }
//        return storageInfoList.filter { checkIfStorageFileIsAvailable(it) }
//    }

    fun deleteFromBucket(storageId: String, storagePath: String, eventData: String) {
        val storageInfo = getAndValidateStorageInfo(storageId, storagePath)
        deleteFile(storageInfo, eventData)
    }

    fun downloadFileFromBucket(storageId: String, storagePath: String, response: HttpServletResponse, eventData: String) {
        val storageInfo = getAndValidateStorageInfo(storageId, storagePath)
        // download
        response.contentType = storageInfo.fileContentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE
        // response.setContentLengthLong(storageFile.fileLength)
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${storageInfo.originalFilename}\"")
        downloadFile(storageInfo, response, eventData) // IMPORTANT: ORDER MATTERS (MUST BE AFTER SETTING HEADER)
    }

    // upload via apache commons fileupload streaming api
    fun uploadViaStreamToBucket(request: HttpServletRequest, isAnonymous: Boolean = false): String {
        val isMultipart = ServletFileUpload.isMultipartContent(request)
        if (!isMultipart) {
            throw ApiException("Invalid Multipart Request", ErrorCode.CLIENT_ERROR, HttpStatus.BAD_REQUEST)
        }
        val storageId = StorageUtils.generateStorageId() // generate storage Id
        uploadFilesViaStream(request, storageId, isAnonymous) // upload file
        return storageId
    }

    private fun getAndValidateStorageInfo(storageId: String, storagePath: String): StorageInfo {
        return if (storageId.isNotEmpty()) {
            storageInfoService.getStorageInfoById(storageId) ?: throw ApiException("Files not found!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
        } else if (storagePath.isNotEmpty()) {
            storageInfoService.getStorageInfoByPath(storagePath) ?: throw ApiException("Files not found!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
        } else {
            throw ApiException("Please provide either storageId or storagePath...", ErrorCode.CLIENT_ERROR, HttpStatus.BAD_REQUEST)
        }
    }

//
//    private fun checkIfStorageFileIsAvailable(storageInfo: StorageInfo): Boolean {
//        if (storageInfo.numOfDownloadsLeft <= 0 || storageInfo.expiryDatetime.isBefore(ZonedDateTime.now())) {
//            // storage expired. Delete the records and objects
//            val storageFiles = storageFileService.getStorageFilesInfoByStorageId(storageInfo.id)
//            if (storageFiles.isEmpty()) {
//                throw ApiException("Files details are not found in database", ErrorCode.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR) // should not occur! If it occurs, means files information in database is missing
//            }
//            deleteFiles(storageFiles, storageInfo)
//            storageFileService.deleteFilesInfo(storageInfo.id)
//            storageInfoService.deleteStorageInfoById(storageInfo.id)
//            return false
//        }
//        return true
//    }
//
//    private fun getAndValidateStorageFiles(storageInfo: StorageInfo): List<StorageFile> {
//        val storageFiles = storageFileService.getStorageFilesInfoByStorageId(storageInfo.id)
//        if (storageFiles.isEmpty()) {
//            throw ApiException("Files not found!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
//        }
//        return storageFiles
//    }
}