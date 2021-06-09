package com.tempfiledrop.storagesvc.service.storage

import com.tempfiledrop.storagesvc.controller.storage.StorageUploadMetadata
import com.tempfiledrop.storagesvc.exception.ApiException
import com.tempfiledrop.storagesvc.exception.ErrorCode
import com.tempfiledrop.storagesvc.service.storagefiles.StorageFile
import com.tempfiledrop.storagesvc.service.storagefiles.StorageFileService
import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfo
import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfoService
import com.tempfiledrop.storagesvc.util.StorageUtils
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
    abstract fun deleteFiles(storageFileList: List<StorageFile>)
    abstract fun downloadFile(storageFile: StorageFile, response: HttpServletResponse)
    abstract fun downloadFilesAsZip(storageFiles: List<StorageFile>, response: HttpServletResponse)

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

    fun getAllStorageInfoFromBucket(bucket: String): List<StorageInfo> {
        val allStorageInfoList = storageInfoService.getStorageInfosInBucket(bucket)
        return allStorageInfoList.filter { checkIfStorageFileIsAvailable(it) }
    }

    fun getStorageInfoFromBucket(bucket: String, storageId: String): StorageInfo {
        return getAndValidateStorageInfo(bucket, storageId)
    }

    fun getMultipleStorageInfoFromBucket(bucket: String, storageIdList: List<String>): List<StorageInfo> {
        val storageInfoList = storageInfoService.getBulkStorageInfoById(storageIdList)
        if (storageInfoList.any { it.bucket != bucket}) {
            throw ApiException("Storage Id not available in Bucket!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
        }
        return storageInfoList.filter { checkIfStorageFileIsAvailable(it) }
    }

    fun deleteFromBucket(bucket: String, storageId: String): StorageInfo {
        val storageInfo = getAndValidateStorageInfo(bucket, storageId)
        val storageFiles = getAndValidateStorageFiles(bucket, storageId)
        deleteFiles(storageFiles)
        storageFileService.deleteFilesInfo(storageId)
        storageInfoService.deleteStorageInfoById(storageId)
        return storageInfo
    }

    fun downloadFilesFromBucket(bucket: String, storageId: String, response: HttpServletResponse): StorageInfo {
        // verify if files are available
        val storageInfo = getAndValidateStorageInfo(bucket, storageId)

        // download
        val storageFiles = getAndValidateStorageFiles(bucket, storageId)
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

    // simple file upload
    fun uploadToBucket(files: List<MultipartFile>, metadata: StorageUploadMetadata? = null): StorageInfo {
        // process files
        val filenames = files.joinToString(",") { it.originalFilename.toString() }
        val storageInfo = if (metadata === null) {
            val anonymousMetadata = StorageUtils.getStorageUploadMetadata(true)
            val expiryDatetime = StorageUtils.processExpiryPeriod(anonymousMetadata.expiryPeriod)
            StorageInfo(anonymousMetadata.bucket, "", filenames, anonymousMetadata.maxDownloads, expiryDatetime)
        } else {
            val storagePath = StorageUtils.processStoragePath(metadata.storagePath) ?: throw ApiException("Storage path is invalid", ErrorCode.UPLOAD_FAILED, HttpStatus.BAD_REQUEST)
            val expiryDatetime = StorageUtils.processExpiryPeriod(metadata.expiryPeriod)
            StorageInfo(metadata.bucket, storagePath, filenames, metadata.maxDownloads, expiryDatetime)
        }

        // store files
        val storageFiles = uploadFiles(files, storageInfo) // upload file
        storageInfoService.addStorageInfo(storageInfo) // store upload to storage mapping. Should populate storage ID after storing
        storageFileService.saveFilesInfo(storageInfo.id.toString(), storageFiles) // store file information

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

    private fun checkIfStorageFileIsAvailable(storageInfo: StorageInfo): Boolean {
        if (storageInfo.numOfDownloadsLeft <= 0 || storageInfo.expiryDatetime.isBefore(ZonedDateTime.now())) {
            // storage expired. Delete the records and objects
            val storageFiles = storageFileService.getStorageFilesInfoByStorageId(storageInfo.id.toString())
            if (storageFiles.isEmpty()) {
                throw ApiException("Files details are not found in database", ErrorCode.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR) // should not occur! If it occurs, means files information in database is missing
            }
            deleteFiles(storageFiles)
            storageFileService.deleteFilesInfo(storageInfo.id.toString())
            storageInfoService.deleteStorageInfoById(storageInfo.id.toString())
            return false
        }
        return true
    }

    private fun getAndValidateStorageFiles(bucket: String, storageId: String): List<StorageFile> {
        val storageFiles = storageFileService.getStorageFilesInfoByStorageId(storageId)
        if (storageFiles.isEmpty()) {
            throw ApiException("Files not found!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
        }
        val anyRecord = storageFiles[0]
        if (anyRecord.bucketName != bucket) {
            throw ApiException("Files not found in Bucket!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST) // bucket and storageID didn't match
        }
        return storageFiles
    }

    private fun getAndValidateStorageInfo(bucket: String, storageId: String): StorageInfo {
        val storageInfo = storageInfoService.getStorageInfoById(storageId) ?: throw ApiException("Files not found!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
        if (storageInfo.bucket != bucket) {
            throw ApiException("Files not found in Bucket!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST) // bucket and storageID didn't match
        }
        if (!checkIfStorageFileIsAvailable(storageInfo)) {
            throw ApiException("Files not available!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
        }
        return storageInfo
    }
}