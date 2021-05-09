package com.tempfiledrop.storagesvc.controller

import com.tempfiledrop.storagesvc.config.StorageSvcProperties
import com.tempfiledrop.storagesvc.exception.ApiException
import com.tempfiledrop.storagesvc.exception.ErrorCode
import com.tempfiledrop.storagesvc.service.storage.StorageService
import com.tempfiledrop.storagesvc.service.storagefiles.StorageFile
import com.tempfiledrop.storagesvc.service.storagefiles.StorageFileServiceImpl
import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfo
import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfoServiceImpl
import com.tempfiledrop.storagesvc.util.StorageUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.ZonedDateTime
import javax.servlet.http.HttpServletResponse
import kotlin.io.path.ExperimentalPathApi

@RestController
@RequestMapping("/storagesvc")
class StorageController(
        private val storageInfoService: StorageInfoServiceImpl,
        private val storageFileService: StorageFileServiceImpl,
        private val storageService: StorageService,
        private val storageSvcProperties: StorageSvcProperties
) {
    companion object {
        private val logger = LoggerFactory.getLogger(StorageController::class.java)
    }

    private fun getStorageFilesAndValidateRequest(bucket: String, storageId: String): List<StorageFile> {
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

    @ExperimentalPathApi
    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
            @RequestPart("files") files: List<MultipartFile>,
            @RequestPart("metadata", required = true) metadata: StorageRequest
    ): ResponseEntity<StorageResponse> {
        // process input
        logger.info("Receiving Request to store ${files.size} files in ${metadata.bucket}/${metadata.storagePath}")
        val storagePath = StorageUtils.processStoragePath(metadata.storagePath) ?: throw ApiException("Storage path is invalid", ErrorCode.UPLOAD_FAILED, HttpStatus.BAD_REQUEST)
        val filenames = files.joinToString(",") { it.originalFilename.toString() }
        val expiryDatetime = StorageUtils.processExpiryPeriod(metadata.expiryPeriod)
        val storageInfo = StorageInfo(metadata.bucket, storagePath, filenames, metadata.maxDownloads, expiryDatetime)

        // store files
        storageService.uploadFiles(files, storageInfo) // upload file
        storageInfoService.addStorageInfo(storageInfo) // store upload to storage mapping. Should populate storage ID after storing
        logger.info("Storage ID == $storageInfo")
        storageFileService.saveFilesInfo(metadata.bucket, storagePath, storageInfo.id.toString(), files) // store file information
        val downloadLink = "${storageSvcProperties.exposeEndpoint}/storagesvc/download/${storageInfo.bucketName}/${storageInfo.id}"
        val response = StorageResponse("Files uploaded successfully", storageInfo.id.toString(), downloadLink)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @DeleteMapping("/{bucket}/{storageId}")
    fun deleteFilesInBucket(@PathVariable("bucket") bucket: String, @PathVariable("storageId") storageId: String): ResponseEntity<StorageResponse> {
        logger.info("Deleting Storage ID = $storageId in Bucket $bucket")
        val storageFiles = getStorageFilesAndValidateRequest(bucket, storageId)

        // delete files
        storageService.deleteFiles(storageFiles)
        storageInfoService.deleteStorageInfoById(storageId)
        storageFileService.deleteFilesInfo(storageId)
        val response = StorageResponse("Files deleted successfully")
        return ResponseEntity(response, HttpStatus.OK)
    }

    @GetMapping("/{bucket}/{storageId}")
    fun getStorageInfoByStorageId(@PathVariable("bucket") bucket: String, @PathVariable("storageId") storageId: String): ResponseEntity<StorageInfoResponse> {
        logger.info("Retrieving Storage Information for $storageId in Bucket $bucket")
        val storageInfo = storageInfoService.getStorageInfoById(storageId) ?: throw ApiException("Files not found!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
        if (storageInfo.bucketName != bucket) {
            throw ApiException("Files not found in Bucket!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST) // bucket and storageID didn't match
        }
        if (storageInfo.numOfDownloadsLeft <= 0 || storageInfo.expiryDatetime.isBefore(ZonedDateTime.now())) {
            throw ApiException("Files not available!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST) // storage have expired (will be scheduled for deletion)
        }

        val downloadLink = "${storageSvcProperties.exposeEndpoint}/storagesvc/download/${storageInfo.bucketName}/${storageInfo.id}"
        val response = StorageInfoResponse(storageInfo.id.toString(), downloadLink, storageInfo.filenames, storageInfo.numOfDownloadsLeft, storageInfo.expiryDatetime)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @GetMapping("/download/{bucket}/{storageId}")
    fun downloadFile(
            @PathVariable("bucket") bucket: String,
            @PathVariable("storageId") storageId: String,
            response: HttpServletResponse
    ) {
        logger.info("Downloading Storage Information for $storageId")
        val storageFiles = getStorageFilesAndValidateRequest(bucket, storageId)
        if(storageFiles.size > 1) {
            // download as zip
            logger.info("Zip File Download...")
            response.status = HttpServletResponse.SC_OK
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${storageId}.zip\"")
            storageService.downloadFilesAsZip(storageFiles, response) // IMPORTANT: ORDER MATTERS (MUST BE AFTER SETTING HEADER)
        } else {
            // download as single file
            logger.info("Single File Download...")
            val storageFile = storageFiles[0]
            response.contentType = storageFile.fileContentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE
            response.setContentLengthLong(storageFile.fileLength)
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${storageFile.filename}\"")
            storageService.downloadFile(storageFile, response) // IMPORTANT: ORDER MATTERS (MUST BE AFTER SETTING HEADER)
        }
        storageInfoService.reduceDownloadCountById(storageId)
    }

    /*
    @GetMapping("/list")
    fun getAllFiles(): ResponseEntity<List<FileInfo>> {
        val fileInfos: List<FileInfo> = service.loadAllFilesFromFolder().map { path ->
            val filename: String = path.fileName.toString()
            val url = MvcUriComponentsBuilder.fromMethodName(StorageController::class.java, "getFile", path.fileName.toString()).build().toString()
            FileInfo(filename, url)
        }.collect(Collectors.toList())
        return ResponseEntity(fileInfos, HttpStatus.OK)
    }
     */
}