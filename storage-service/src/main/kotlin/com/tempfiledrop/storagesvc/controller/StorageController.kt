package com.tempfiledrop.storagesvc.controller

import com.tempfiledrop.storagesvc.config.StorageSvcProperties
import com.tempfiledrop.storagesvc.exception.ApiException
import com.tempfiledrop.storagesvc.exception.ErrorCode
import com.tempfiledrop.storagesvc.service.storage.StorageService
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
import javax.servlet.http.HttpServletResponse
import kotlin.io.path.ExperimentalPathApi

@RestController
@RequestMapping("/storagesvc")
class StorageController(
        private val storageInfoService: StorageInfoServiceImpl,
        private val storageService: StorageService,
        private val storageSvcProperties: StorageSvcProperties
) {
    companion object {
        private val logger = LoggerFactory.getLogger(StorageController::class.java)
    }

    private fun getStorageInfoListAndValidateStorageIdRequest(bucket: String, storageId: String): List<StorageInfo> {
        val storageInfoList = storageInfoService.getStorageInfosByStorageId(storageId)
        if (storageInfoList.isEmpty()) {
            throw ApiException("Files not found!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
        }
        val anyRecord = storageInfoList[0]
        if (anyRecord.bucketName != bucket) {
            throw ApiException("Files not found in Bucket!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST) // bucket and storageID didn't match
        }
        return storageInfoList
    }

    @ExperimentalPathApi
    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
            @RequestPart("files") files: List<MultipartFile>,
            @RequestPart("metadata", required = true) metadata: StorageRequest
    ): ResponseEntity<StorageResponse> {
        // process input
        logger.info("Receiving Request to store ${files.size} files in ${metadata.bucket}/${metadata.storagePath}")
        val (isValidStoragePath, storagePath) = StorageUtils.processStoragePath(metadata.storagePath)
        // validate path
        if (!isValidStoragePath) {
            throw ApiException("Storage path is invalid", ErrorCode.UPLOAD_FAILED, HttpStatus.BAD_REQUEST)
        }
        val storageInfo = StorageInfo(metadata.bucket, storagePath!!)

        // store files
        storageService.uploadFiles(files, storageInfo)
        val storageId = storageInfoService.addStorageInfo(files, storageInfo)
        val downloadLink = "${storageSvcProperties.exposeEndpoint}/storagesvc/download/${storageInfo.bucketName}/$storageId"
        val response = StorageResponse("Files uploaded successfully", storageId, downloadLink)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @DeleteMapping("/{bucket}/{storageId}")
    fun deleteFilesInBucket(@PathVariable("bucket") bucket: String, @PathVariable("storageId") storageId: String): ResponseEntity<StorageResponse> {
        logger.info("Deleting Storage ID = $storageId in Bucket $bucket")
        val storageInfoList = storageInfoService.getStorageInfosByStorageId(storageId)
        if (storageInfoList.isEmpty()) {
            throw ApiException("Files not found!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST) // no files available
        }
        if (storageInfoList[0].bucketName != bucket) {
            throw ApiException("Files not found in Bucket!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST) // bucket and storageID didn't match
        }

        // delete files
        storageService.deleteFiles(storageInfoList)
        storageInfoService.deleteStorageInfoById(storageId)
        val response = StorageResponse("Files deleted successfully")
        return ResponseEntity(response, HttpStatus.OK)
    }

    @GetMapping("/{bucket}/{storageId}")
    fun getStorageInfoByStorageId(@PathVariable("bucket") bucket: String, @PathVariable("storageId") storageId: String): ResponseEntity<StorageInfoResponse> {
        logger.info("Retrieving Storage Information for $storageId in Bucket $bucket")
        val storageInfoList = getStorageInfoListAndValidateStorageIdRequest(bucket, storageId)
        val anyRecord = storageInfoList[0]
        val downloadLink = "${storageSvcProperties.exposeEndpoint}/storagesvc/download/${anyRecord.bucketName}/${anyRecord.storageId}"
        val filenames = storageInfoList.map { it.getFullStoragePath() }
        val response = StorageInfoResponse(anyRecord.storageId, downloadLink, filenames)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @GetMapping("/download/{bucket}/{storageId}")
    fun downloadFile(
            @PathVariable("bucket") bucket: String,
            @PathVariable("storageId") storageId: String,
            response: HttpServletResponse
    ) {
        logger.info("Downloading Storage Information for $storageId")
        val storageInfoList = getStorageInfoListAndValidateStorageIdRequest(bucket, storageId)
        if(storageInfoList.size > 1) {
            // download as zip
            logger.info("Zip File Download...")
            response.status = HttpServletResponse.SC_OK
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${storageId}.zip\"")
            storageService.downloadFilesAsZip(storageInfoList, response) // IMPORTANT: ORDER MATTERS (MUST BE AFTER SETTING HEADER)
        } else {
            // download as single file
            logger.info("Single File Download...")
            val storageInfo = storageInfoList[0]
            response.contentType = storageInfo.storageFileContentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE
            response.setContentLengthLong(storageInfo.storageFileLength)
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${storageInfo.storageFilename}\"")
            storageService.downloadFile(storageInfo, response) // IMPORTANT: ORDER MATTERS (MUST BE AFTER SETTING HEADER)
        }
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