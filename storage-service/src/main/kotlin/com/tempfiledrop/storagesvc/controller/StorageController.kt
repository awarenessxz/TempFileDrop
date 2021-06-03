package com.tempfiledrop.storagesvc.controller

import com.tempfiledrop.storagesvc.config.StorageSvcProperties
import com.tempfiledrop.storagesvc.exception.ApiException
import com.tempfiledrop.storagesvc.exception.ErrorCode
import com.tempfiledrop.storagesvc.exception.ErrorResponse
import com.tempfiledrop.storagesvc.service.event.EventType
import com.tempfiledrop.storagesvc.service.event.RabbitMQProducer
import com.tempfiledrop.storagesvc.service.storage.StorageService
import com.tempfiledrop.storagesvc.service.storagefiles.StorageFile
import com.tempfiledrop.storagesvc.service.storagefiles.StorageFileService
import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfo
import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfoService
import com.tempfiledrop.storagesvc.util.StorageUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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
        private val storageInfoService: StorageInfoService,
        private val storageFileService: StorageFileService,
        private val storageService: StorageService,
        private val storageSvcProperties: StorageSvcProperties,
        private val producer: RabbitMQProducer
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

    private fun checkIfStorageFileIsAvailable(storageInfo: StorageInfo): Boolean {
        if (storageInfo.numOfDownloadsLeft <= 0 || storageInfo.expiryDatetime.isBefore(ZonedDateTime.now())) {
            // storage expired. Delete the records and objects
            val storageFiles = storageFileService.getStorageFilesInfoByStorageId(storageInfo.id.toString())
            if (storageFiles.isEmpty()) {
                throw ApiException("Files details are not found in database", ErrorCode.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR) // should not occur! If it occurs, means files information in database is missing
            }
            storageService.deleteFiles(storageFiles)
            storageFileService.deleteFilesInfo(storageInfo.id.toString())
            storageInfoService.deleteStorageInfoById(storageInfo.id.toString())
            return false
        }
        return true
    }

    private fun getDownloadLink(bucket: String, storageId: String): String {
        return "${storageSvcProperties.exposeEndpoint}/storagesvc/download/$bucket/$storageId"
    }

    @GetMapping("/{bucket}")
    fun getStorageInBucket(@PathVariable("bucket") bucket: String): ResponseEntity<List<StorageInfoResponse>> {
        logger.info("Receiving Request to get content inside $bucket")
        val storageInfoList = storageInfoService.getStorageInfosInBucket(bucket)
        val response = storageInfoList
                .filter { checkIfStorageFileIsAvailable(it) }
                .map { StorageInfoResponse(it.id.toString(), getDownloadLink(it.bucketName, it.id.toString()), it.filenames, it.numOfDownloadsLeft, it.expiryDatetime) }
        return ResponseEntity(response, HttpStatus.OK)
    }

    @Operation(summary = "Upload single or multiple files to storage service")
    @ApiResponses(value = [
        ApiResponse(description = "File were uploaded successfully", responseCode = "200"),
        ApiResponse(description = "Requested upload path is invalid", responseCode = "400", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(description = "Upload Failed", responseCode = "500", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ])
    @ExperimentalPathApi
    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = ["application/json"])
    fun uploadFile(
            @RequestPart("files") files: List<MultipartFile>,
            @RequestPart("metadata", required = true) metadata: StorageUploadRequest
    ): ResponseEntity<StorageUploadResponse> {
        // process input
        logger.info("Receiving Request to store ${files.size} files in ${metadata.bucket}/${metadata.storagePath}")
        val storagePath = StorageUtils.processStoragePath(metadata.storagePath) ?: throw ApiException("Storage path is invalid", ErrorCode.UPLOAD_FAILED, HttpStatus.BAD_REQUEST)
        val filenames = files.joinToString(",") { it.originalFilename.toString() }
        val expiryDatetime = StorageUtils.processExpiryPeriod(metadata.expiryPeriod)
        val storageInfo = StorageInfo(metadata.bucket, storagePath, filenames, metadata.maxDownloads, expiryDatetime)

        // store files
        val storageFiles = storageService.uploadFiles(files, storageInfo) // upload file
        storageInfoService.addStorageInfo(storageInfo) // store upload to storage mapping. Should populate storage ID after storing
        storageFileService.saveFilesInfo(storageInfo.id.toString(), storageFiles) // store file information
        val downloadLink = "${storageSvcProperties.exposeEndpoint}/storagesvc/download/${storageInfo.bucketName}/${storageInfo.id}"

        // send an event
        producer.sendEventwithHeader(EventType.FILE_UPLOADED, storageInfo, metadata.eventData, metadata.eventRoutingKey)

        // send response
        val response = StorageUploadResponse("Files uploaded successfully", storageInfo.id.toString(), downloadLink)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @Operation(summary = "Delete files based on bucket name and storageId")
    @ApiResponses(value = [
        ApiResponse(description = "File were deleted successfully", responseCode = "200", content = [Content(mediaType = "string")]),
        ApiResponse(description = "Files not found", responseCode = "400", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))])
    ])
    @DeleteMapping("/{bucket}/{storageId}")
    fun deleteFilesInBucket(@PathVariable("bucket") bucket: String, @PathVariable("storageId") storageId: String): ResponseEntity<String> {
        logger.info("Deleting Storage ID = $storageId in Bucket $bucket")
        val storageFiles = getStorageFilesAndValidateRequest(bucket, storageId)
        storageService.deleteFiles(storageFiles)
        storageFileService.deleteFilesInfo(storageId)
        storageInfoService.deleteStorageInfoById(storageId)
        return ResponseEntity("Files deleted successfully", HttpStatus.OK)
    }

    @Operation(summary = "Get Storage Information of uploaded files using bucket name and storageId")
    @ApiResponses(value = [
        ApiResponse(description = "Successful operation", responseCode = "200"),
        ApiResponse(description = "Files not found", responseCode = "400", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @GetMapping("/storageinfo/{bucket}/{storageId}", produces = ["application/json"])
    fun getStorageInfoByStorageId(@PathVariable("bucket") bucket: String, @PathVariable("storageId") storageId: String): ResponseEntity<StorageInfoResponse> {
        logger.info("Retrieving Storage Information for $storageId in Bucket $bucket")
        val storageInfo = storageInfoService.getStorageInfoById(storageId) ?: throw ApiException("Files not found!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
        if (storageInfo.bucketName != bucket) {
            throw ApiException("Files not found in Bucket!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST) // bucket and storageID didn't match
        }
        if (!checkIfStorageFileIsAvailable(storageInfo)) {
            throw ApiException("Files not available!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
        }
        val response = StorageInfoResponse(storageInfo.id.toString(), getDownloadLink(storageInfo.bucketName, storageInfo.id.toString()), storageInfo.filenames, storageInfo.numOfDownloadsLeft, storageInfo.expiryDatetime)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @Operation(summary = "Get Multiple Storage Information of uploaded files using bucket name and Bulk storageId")
    @ApiResponses(value = [
        ApiResponse(description = "Successful operation", responseCode = "200"),
        ApiResponse(description = "Files not found", responseCode = "400", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @PostMapping("/storageinfo/bulk")
    fun getMultipleStorageInfoByStorageId(@RequestBody storageInfoReq: StorageInfoBulkRequest): ResponseEntity<List<StorageInfoResponse>> {
        logger.info("Retrieving Bulk StorageInfo Request for ${storageInfoReq.storageIdList} in Bucket ${storageInfoReq.bucket}")
        val storageInfoList = storageInfoService.getBulkStorageInfoById(storageInfoReq.storageIdList)
        if (storageInfoList.any { it.bucketName != storageInfoReq.bucket}) {
            throw ApiException("Storage Id not available in Bucket!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST)
        }
        val availableStorageInfoResponseList = storageInfoList
                .filter { checkIfStorageFileIsAvailable(it) }
                .map { StorageInfoResponse(it.id.toString(), getDownloadLink(it.bucketName, it.id.toString()), it.filenames, it.numOfDownloadsLeft, it.expiryDatetime) }
        return ResponseEntity(availableStorageInfoResponseList, HttpStatus.OK)
    }

    @Operation(summary = "Download files using bucket name and storageId")
    @ApiResponses(value = [
        ApiResponse(description = "Successful operation", responseCode = "200"),
        ApiResponse(description = "Files not found", responseCode = "400", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))])
    ])
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
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"download.zip\"")
            storageService.downloadFilesAsZip(storageFiles, response) // IMPORTANT: ORDER MATTERS (MUST BE AFTER SETTING HEADER)
        } else {
            // download as single file
            logger.info("Single File Download...")
            val storageFile = storageFiles[0]
            response.contentType = storageFile.fileContentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE
            response.setContentLengthLong(storageFile.fileLength)
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${storageFile.originalFilename}\"")
            storageService.downloadFile(storageFile, response) // IMPORTANT: ORDER MATTERS (MUST BE AFTER SETTING HEADER)
        }
        storageInfoService.reduceDownloadCountById(storageId)
    }
}