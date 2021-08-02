package com.tempstorage.storagesvc.controller.storage

import com.tempstorage.storagesvc.controller.SecuredRestController
import com.tempstorage.storagesvc.exception.ErrorResponse
import com.tempstorage.storagesvc.service.notification.EventType
import com.tempstorage.storagesvc.service.notification.RabbitMQProducer
import com.tempstorage.storagesvc.service.eventdata.EventData
import com.tempstorage.storagesvc.service.eventdata.EventDataService
import com.tempstorage.storagesvc.service.storage.StorageService
import com.tempstorage.storagesvc.service.storage.FileSystemNode
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
import kotlin.io.path.ExperimentalPathApi

@RestController
@RequestMapping("/api/storagesvc")
class StorageController(
        private val storageService: StorageService,
        private val eventDataService: EventDataService,
        private val producer: RabbitMQProducer
): SecuredRestController {
    companion object {
        private val logger = LoggerFactory.getLogger(StorageController::class.java)
    }

    @Operation(summary = "Get number of files in bucket")
    @GetMapping("/{bucket}/count")
    fun getNumberOfFilesInBucket(@PathVariable("bucket") bucket: String): ResponseEntity<Int> {
        logger.info("Receiving Request to get number of files inside $bucket")
        val count = storageService.getAllStorageInfo(bucket).size
        return ResponseEntity(count, HttpStatus.OK)
    }

    @Operation(summary = "Get all files and folders inside bucket in folder like structure")
    @GetMapping("/{bucket}")
    fun getStorageFromBucket(@PathVariable("bucket") bucket: String): ResponseEntity<FileSystemNode> {
        logger.info("Receiving Request to get content inside $bucket")
        val filesystemNode = storageService.listFilesAndFoldersInBucket(bucket)
        return ResponseEntity(filesystemNode, HttpStatus.OK)
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

        // store files
        val (metadata, storageInfo) = storageService.uploadViaStreamToBucket(request)

        // send an event
        producer.sendEventwithHeader(EventType.FILES_UPLOADED, storageInfo, metadata.eventData!!, metadata.eventRoutingKey, true)

        // send response
        val response = StorageUploadResponse("Files uploaded successfully", storageInfo.id.toString())
        return ResponseEntity(response, HttpStatus.OK)
    }

    @Operation(summary = "Delete files based on bucket name and storageId")
    @ApiResponses(value = [
        ApiResponse(description = "File were deleted successfully", responseCode = "200", content = [Content(mediaType = "string")]),
        ApiResponse(description = "Files not found", responseCode = "400", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))])
    ])
    @DeleteMapping("/{bucket}/{storageId}")
    fun deleteFilesInBucket(
            @PathVariable("bucket") bucket: String,
            @PathVariable("storageId") storageId: String,
            @RequestParam(required = false, defaultValue = "") eventData: String,
            @RequestParam(required = false, defaultValue = "") eventRoutingKey: String,
    ): ResponseEntity<String> {
        logger.info("Deleting Storage ID = $storageId in Bucket $bucket")

        // delete files
        val storageInfo = storageService.deleteFromBucket(bucket, storageId)

        // send an event
        producer.sendEventwithHeader(EventType.FILES_DELETED, storageInfo, eventData, eventRoutingKey, true)

        // send response
        return ResponseEntity("Files deleted successfully", HttpStatus.OK)
    }

    @Operation(summary = "Get list of storage information from bucket")
    @GetMapping("/storageinfo/{bucket}")
    fun getAllStorageInfoInBucket(@PathVariable("bucket") bucket: String): ResponseEntity<List<StorageInfoResponse>> {
        logger.info("Receiving Request to get all StorageInfo inside $bucket")
        val storageInfoList = storageService.getAllStorageInfo(bucket)
        val response = storageInfoList.map { StorageInfoResponse(it.id.toString(), it.filenames, it.numOfDownloadsLeft, it.expiryDatetime, it.allowAnonymousDownload) }
        return ResponseEntity(response, HttpStatus.OK)
    }

    @Operation(summary = "Get Storage Information of uploaded files using bucket name and storageId")
    @ApiResponses(value = [
        ApiResponse(description = "Successful operation", responseCode = "200"),
        ApiResponse(description = "Files not found", responseCode = "400", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @GetMapping("/storageinfo/{bucket}/{storageId}", produces = ["application/json"])
    fun getStorageInfoByStorageId(@PathVariable("bucket") bucket: String, @PathVariable("storageId") storageId: String): ResponseEntity<StorageInfoResponse> {
        logger.info("Retrieving Storage Information for $storageId in Bucket $bucket")
        val storageInfo = storageService.getStorageInfoFromBucket(bucket, storageId)
        val response = StorageInfoResponse(storageId, storageInfo.filenames, storageInfo.numOfDownloadsLeft, storageInfo.expiryDatetime, storageInfo.allowAnonymousDownload)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @Operation(summary = "Get Multiple Storage Information of uploaded files using bucket name and Bulk storageId")
    @ApiResponses(value = [
        ApiResponse(description = "Successful operation", responseCode = "200"),
        ApiResponse(description = "Files not found", responseCode = "400", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @PostMapping("/storageinfo/bulk")
    fun getMultipleStorageInfoByStorageId(@RequestBody storageInfoReq: StorageInfoBulkRequest): ResponseEntity<StorageInfoBulkResponse> {
        logger.info("Retrieving Bulk StorageInfo Request for ${storageInfoReq.storageIdList} in Bucket ${storageInfoReq.bucket}")
        val storageInfoList = storageService.getMultipleStorageInfoFromBucket(storageInfoReq.bucket, storageInfoReq.storageIdList)
        val result = storageInfoList.map { StorageInfoResponse(it.id.toString(), it.filenames, it.numOfDownloadsLeft, it.expiryDatetime, it.allowAnonymousDownload) }
        val response = StorageInfoBulkResponse(result)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @Operation(summary = "Get all events that were published to message queue for bucket")
    @GetMapping("/events/{bucket}")
    fun getAllEvents(@PathVariable("bucket") bucket: String): ResponseEntity<List<EventData>> {
        logger.info("Receiving Request to get all events that were published to message queue for $bucket")
        val events = eventDataService.getEventsByBucket(bucket)
        return ResponseEntity(events, HttpStatus.OK)
    }
}