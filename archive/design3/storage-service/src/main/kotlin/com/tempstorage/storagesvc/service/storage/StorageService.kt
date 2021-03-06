package com.tempstorage.storagesvc.service.storage

import com.tempstorage.storagesvc.controller.storage.StorageUploadMetadata
import com.tempstorage.storagesvc.exception.ApiException
import com.tempstorage.storagesvc.exception.ErrorCode
import com.tempstorage.storagesvc.service.storagefiles.StorageFile
import com.tempstorage.storagesvc.service.storagefiles.StorageFileService
import com.tempstorage.storagesvc.service.storageinfo.StorageInfo
import com.tempstorage.storagesvc.service.storageinfo.StorageInfoService
import com.tempstorage.storagesvc.util.StorageUtils
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.multipart.MultipartFile
import java.time.ZonedDateTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

abstract class StorageService {
    companion object {
        private val logger = LoggerFactory.getLogger(StorageService::class.java)
    }

    abstract fun initStorage()
    abstract fun uploadFiles(files: List<MultipartFile>, storageInfo: StorageInfo): List<StorageFile>
    abstract fun uploadFilesViaStream(request: HttpServletRequest, isAnonymous: Boolean): Triple<StorageUploadMetadata, StorageInfo, List<StorageFile>>
    abstract fun deleteFiles(storageFileList: List<StorageFile>, bucket: String)
    abstract fun downloadFile(storageFile: StorageFile, response: HttpServletResponse)
    abstract fun downloadFilesAsZip(storageFiles: List<StorageFile>, response: HttpServletResponse)
    abstract fun getAllFileSizeInBucket(bucket: String, storageFiles: List<StorageFile>): List<StorageFile>

    private lateinit var storageInfoService: StorageInfoService
    private lateinit var storageFileService: StorageFileService

    @Autowired
    fun setStorageInfoService(storageInfoService: StorageInfoService) {
        this.storageInfoService = storageInfoService
    }

    @Autowired
    fun setStorageFileService(storageFileService: StorageFileService) {
        this.storageFileService = storageFileService
    }

    fun getAllBuckets(): List<String> {
        return storageInfoService.getBuckets()
    }

    fun getAllStorageInfo(bucket: String? = null): List<StorageInfo> {
        var allStorageInfoList = storageInfoService.getAllStorageInfo()
        if (bucket != null) {
            StorageUtils.validateBucketWithJwtToken(bucket)
            allStorageInfoList = allStorageInfoList.filter { it.bucket == bucket }
        }
        return allStorageInfoList.filter { checkIfStorageFileIsAvailable(it) }
    }

    // convert list of all storage in bucket into folder like structure
    fun listFilesAndFoldersInBucket(bucket: String): FileSystemNode {
        val storageInfoList = getAllStorageInfo(bucket)
        val storageIds = storageInfoList.map { it.id.toString() }
        val storageFiles = storageFileService.getStorageFilesInfoByStorageIdBulk(storageIds)
        val storageFilesWithFileSize = getAllFileSizeInBucket(bucket, storageFiles)

        val filesystemNodes = storageInfoList.map { storageInfo ->
            val storageSize = storageFilesWithFileSize.filter { it.storageId == storageInfo.id.toString() }.sumBy { it.fileLength.toInt() }
            FileSystemNode(true, storageInfo.filenames, "${storageInfo.storagePath}/${storageInfo.filenames}", storageInfo.bucket, storageInfo.id.toString(), storageSize, storageInfo.numOfDownloadsLeft, storageInfo.expiryDatetime)
        }
        return StorageUtils.buildFolderTreeStructure(bucket, filesystemNodes)
    }

    fun getStorageInfoFromBucket(bucket: String, storageId: String): StorageInfo {
        return getAndValidateStorageInfo(storageId, bucket)
    }

    fun getMultipleStorageInfoFromBucket(bucket: String, storageIdList: List<String>): List<StorageInfo> {
        StorageUtils.validateBucketWithJwtToken(bucket)
        val storageInfoList = storageInfoService.getBulkStorageInfoById(storageIdList)
        if (storageInfoList.any { it.bucket != bucket}) {
            throw ApiException("Storage Id not available in Bucket!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
        }
        return storageInfoList.filter { checkIfStorageFileIsAvailable(it) }
    }

    fun deleteFromBucket(bucket: String, storageId: String): StorageInfo {
        val storageInfo = getAndValidateStorageInfo(storageId, bucket)
        val storageFiles = storageFileService.getStorageFilesInfoByStorageId(storageId)
        deleteFiles(storageFiles, storageInfo.bucket)
        storageFileService.deleteFilesInfo(storageId)
        storageInfoService.deleteStorageInfoById(storageId)
        return storageInfo
    }

    fun downloadFilesFromBucket(storageId: String, response: HttpServletResponse, isAuth: Boolean = false, bucket: String? = null): StorageInfo {
        // verify if files are available
        val storageInfo = getAndValidateStorageInfo(storageId, bucket)
        val storageFiles = getAndValidateStorageFiles(storageInfo)
        if (!isAuth && !storageInfo.allowAnonymousDownload) {
            throw ApiException("Download requires authentication", ErrorCode.DOWNLOAD_DENIED, HttpStatus.UNAUTHORIZED)
        }

        // download
        if(storageFiles.size > 1) {
            // download as zip
            logger.info("Zip File Download...")
            response.status = HttpServletResponse.SC_OK
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"download.zip\"")
            downloadFilesAsZip(storageFiles, response) // IMPORTANT: ORDER MATTERS (MUST BE AFTER SETTING HEADER)
        } else {
            // download as single file
            logger.info("Single File Download...")
            val storageFile = storageFiles[0]
            response.contentType = storageFile.fileContentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE
            // response.setContentLengthLong(storageFile.fileLength)
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${storageFile.originalFilename}\"")
            downloadFile(storageFile, response) // IMPORTANT: ORDER MATTERS (MUST BE AFTER SETTING HEADER)
        }

        // process storage info
        storageInfoService.reduceDownloadCountById(storageId)

        return storageInfo
    }

    // upload via apache commons fileupload streaming api
    fun uploadViaStreamToBucket(request: HttpServletRequest, isAnonymous: Boolean = false): Pair<StorageUploadMetadata, StorageInfo> {
        val isMultipart = ServletFileUpload.isMultipartContent(request)
        if (!isMultipart) {
            throw ApiException("Invalid Multipart Request", ErrorCode.CLIENT_ERROR, HttpStatus.BAD_REQUEST)
        }
        val (metadata, storageInfo, storageFiles) = uploadFilesViaStream(request, isAnonymous) // upload files
        storageInfoService.addStorageInfo(storageInfo) // store upload to storage mapping. Should populate storage ID after storing
        storageFileService.saveFilesInfo(storageInfo.id.toString(), storageFiles) // store file information
        return Pair(metadata, storageInfo)
    }

    fun getAndValidateStorageInfo(storageId: String, bucket: String? = null): StorageInfo {
        // validate bucket first
        if (bucket != null) {
            StorageUtils.validateBucketWithJwtToken(bucket)
        }
        // retrieve storage information
        val storageInfo = storageInfoService.getStorageInfoById(storageId) ?: throw ApiException("Files not found!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
        // validate if bucket matches storage Id
        bucket?.let {
            if (storageInfo.bucket != bucket) {
                throw ApiException("Files not found in Bucket!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST) // bucket and storageID didn't match
            }
        }
        if (!checkIfStorageFileIsAvailable(storageInfo)) {
            throw ApiException("Files not available!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
        }
        return storageInfo
    }

    private fun checkIfStorageFileIsAvailable(storageInfo: StorageInfo): Boolean {
        if (storageInfo.numOfDownloadsLeft <= 0 || storageInfo.expiryDatetime.isBefore(ZonedDateTime.now())) {
            // storage expired. Delete the records and objects
            val storageFiles = storageFileService.getStorageFilesInfoByStorageId(storageInfo.id.toString())
            if (storageFiles.isEmpty()) {
                throw ApiException("Files details are not found in database", ErrorCode.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR) // should not occur! If it occurs, means files information in database is missing
            }
            deleteFiles(storageFiles, storageInfo.bucket)
            storageFileService.deleteFilesInfo(storageInfo.id.toString())
            storageInfoService.deleteStorageInfoById(storageInfo.id.toString())
            return false
        }
        return true
    }

    private fun getAndValidateStorageFiles(storageInfo: StorageInfo): List<StorageFile> {
        val storageFiles = storageFileService.getStorageFilesInfoByStorageId(storageInfo.id.toString())
        if (storageFiles.isEmpty()) {
            throw ApiException("Files not found!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
        }
        return storageFiles
    }
}