package com.tempstorage.storagesvc.controller.storage

import com.tempstorage.storagesvc.exception.ErrorResponse
import com.tempstorage.storagesvc.service.storage.StorageService
import io.minio.http.Method
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.io.path.ExperimentalPathApi

@RestController
@RequestMapping("/api/storagesvc")
class StorageController(
        private val storageService: StorageService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(StorageController::class.java)
    }
//
//    @Operation(summary = "Get number of files in bucket")
//    @GetMapping("/{bucket}/count")
//    fun getNumberOfFilesInBucket(@PathVariable("bucket") bucket: String): ResponseEntity<Int> {
//        logger.info("Receiving Request to get number of files inside $bucket")
//        val count = storageService.getAllStorageInfoFromBucket(bucket).size
//        return ResponseEntity(count, HttpStatus.OK)
//    }
//
//    @Operation(summary = "Get all files and folders inside bucket in folder like structure")
//    @GetMapping("/tree/{bucket}")
//    fun getStorageFromBucket(@PathVariable("bucket") bucket: String): ResponseEntity<FileSystemNode> {
//        logger.info("Receiving Request to get content inside $bucket")
//        val filesystemNode = storageService.listFilesAndFoldersInBucket(bucket)
//        return ResponseEntity(filesystemNode, HttpStatus.OK)
//    }
//
//    @Operation(summary = "Get all files in bucket")
//    @GetMapping("/{bucket}")
//    fun getAllStorageInfoInBucket(@PathVariable("bucket") bucket: String): ResponseEntity<List<StorageInfo>> {
//        logger.info("Receiving Request to get all StorageInfo inside $bucket")
//        val storageInfoList = storageService.getAllStorageInfoFromBucket(bucket)
//        return ResponseEntity(storageInfoList, HttpStatus.OK)
//    }
//
//    @Operation(summary = "Get Storage Information based on storageId or storagePath")
//    @ApiResponses(value = [
//        ApiResponse(description = "Successful operation", responseCode = "200"),
//        ApiResponse(description = "Files not found", responseCode = "400", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
//    ])
//    @GetMapping("/storageinfo", produces = ["application/json"])
//    fun getStorageInfoByStorageId(
//            @RequestParam(value = "storageId", required = false, defaultValue = "") storageId: String,
//            @RequestParam(value = "storagePath", required = false, defaultValue = "") storagePath: String
//    ): ResponseEntity<StorageInfo> {
//        logger.info("Retrieving file information....")
//        val storageInfo = storageService.getStorageInfoFromBucket(storageId, storagePath)
//        // TODO: Implement checks to see if storageId can be retrieved by anonymous users
//        return ResponseEntity(storageInfo, HttpStatus.OK)
//    }
//
//    @Operation(summary = "Get Multiple Storage Information based on list of storageId")
//    @ApiResponses(value = [
//        ApiResponse(description = "Successful operation", responseCode = "200"),
//        ApiResponse(description = "Files not found", responseCode = "400", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
//    ])
//    @PostMapping("/storageinfo/bulk")
//    fun getMultipleStorageInfoByStorageId(@RequestBody storageInfoBulkReq: StorageInfoBulkRequest): ResponseEntity<StorageInfoBulkResponse> {
//        logger.info("Bulk request for retrieving file information....")
//        val storageInfoList = storageInfoBulkReq.storageIdList.map { storageService.getStorageInfoFromBucket(it, "", false) }
//        return ResponseEntity(StorageInfoBulkResponse(storageInfoList), HttpStatus.OK)
//    }

    /***************************************************************************************************************************************************************
     * Upload Endpoints
     ***************************************************************************************************************************************************************/

    @Operation(summary = "Get S3 Upload Endpoints")
    @ApiResponses(value = [
        ApiResponse(description = "Successful operation", responseCode = "200")
    ])
    @GetMapping("/s3-upload-url")
    fun getS3UploadUrl(params: StorageS3PresignedUrlParams): ResponseEntity<StorageS3PresignedUrlResponse> {
        logger.info("Preparing Url to upload ${params.storageObjects} to ${params.bucket}...")
        // TODO: VALIDATE if user have access to bucket
        val response = storageService.generateS3PresignedUrl(params, Method.PUT)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @Operation(summary = "Upload single or multiple files to storage service")
    @ApiResponses(value = [
        ApiResponse(description = "File were uploaded successfully", responseCode = "200"),
        ApiResponse(description = "Requested upload path is invalid", responseCode = "400", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(description = "Upload Failed", responseCode = "500", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ])
    @ExperimentalPathApi
    @PostMapping("/upload")
    fun uploadFilesViaStream(request: HttpServletRequest): ResponseEntity<StorageUploadResponse> {
        logger.info("Receiving upload request via stream")
        val response = storageService.uploadViaStreamToBucket(request) // store files
        return ResponseEntity(response, HttpStatus.OK)
    }

    /***************************************************************************************************************************************************************
     * Download Endpoints
     ***************************************************************************************************************************************************************/

    @Operation(summary = "Get S3 Download Endpoints")
    @ApiResponses(value = [
        ApiResponse(description = "Successful operation", responseCode = "200")
    ])
    @GetMapping("/s3-download-url")
    fun getS3DownloadUrl(params: StorageS3PresignedUrlParams): ResponseEntity<StorageS3PresignedUrlResponse> {
        logger.info("Preparing Url to download ${params.storageObjects} from ${params.bucket}...")
        // TODO: VALIDATE if user have access to bucket
        val response = storageService.generateS3PresignedUrl(params, Method.GET)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @Operation(summary = "Download single or multiple files")
    @ApiResponses(value = [
        ApiResponse(description = "Successful operation", responseCode = "200"),
        ApiResponse(description = "Files not found", responseCode = "400", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ])
    @GetMapping("/download")
    fun downloadFiles(
            @RequestParam(value = "storageObjects", required = true, defaultValue = "") storageObjects: List<String>,
            response: HttpServletResponse
    ) {
        logger.info("Downloading $storageObjects....")
        storageService.downloadFilesFromBucket(storageObjects, response)
    }

    /***************************************************************************************************************************************************************
     * Delete Endpoints
     ***************************************************************************************************************************************************************/

    @Operation(summary = "Delete files using objectName")
    @ApiResponses(value = [
        ApiResponse(description = "Files were deleted successfully", responseCode = "200", content = [Content(mediaType = "string")]),
        ApiResponse(description = "Files not found", responseCode = "400", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))])
    ])
    @DeleteMapping("/")
    fun deleteFilesInBucket(
            @RequestParam(value = "storageObjects", required = true, defaultValue = "") storageObjects: List<String>,
    ): ResponseEntity<Map<String, String>> {
        logger.info("Deleting $storageObjects....")
        val response = storageService.deleteFilesInBucket(storageObjects)
        return ResponseEntity(response, HttpStatus.OK)
    }
}
