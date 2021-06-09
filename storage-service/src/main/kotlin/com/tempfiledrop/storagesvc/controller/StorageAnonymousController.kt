package com.tempfiledrop.storagesvc.controller

import com.tempfiledrop.storagesvc.config.StorageSvcProperties
import com.tempfiledrop.storagesvc.exception.ErrorResponse
import com.tempfiledrop.storagesvc.service.event.EventType
import com.tempfiledrop.storagesvc.service.storage.StorageService
import com.tempfiledrop.storagesvc.util.StorageUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.io.path.ExperimentalPathApi

@RestController
@RequestMapping("/api/storagesvc/anonymous")
@ConditionalOnProperty(prefix = "tempfiledrop.storagesvc.anonymous-upload", name = ["enable"], havingValue = "true")
class StorageAnonymousController(
        private val storageService: StorageService,
        private val storagesvcProps: StorageSvcProperties
) {
    companion object {
        private val logger = LoggerFactory.getLogger(StorageAnonymousController::class.java)
    }

    @Operation(summary = "Upload single or multiple files to storage service anonymously")
    @ApiResponses(value = [
        ApiResponse(description = "File were uploaded successfully", responseCode = "200"),
        ApiResponse(description = "Requested upload path is invalid", responseCode = "400", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(description = "Upload Failed", responseCode = "500", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ])
    @ExperimentalPathApi
    @PostMapping("/upload")
    fun uploadFilesViaStream(request: HttpServletRequest): ResponseEntity<StorageUploadResponse> {
        logger.info("Receiving anonymous upload request via stream")
        // verify file size (Some issues with this approach where uploading connection breaks which prevents frontend from handling error gracefully)
        if (request.contentLengthLong >= storagesvcProps.anonymousUpload.maxFileSize) {
            throw MaxUploadSizeExceededException(storagesvcProps.anonymousUpload.maxFileSize)
        }

        // store files
        val (_, storageInfo) = storageService.uploadViaStreamToBucket(request, true)

        // send response
        val response = StorageUploadResponse("Files uploaded successfully", storageInfo.id.toString())
        return ResponseEntity(response, HttpStatus.OK)
    }

    @Operation(summary = "Get Storage Information of uploaded files using storageId anonymously")
    @ApiResponses(value = [
        ApiResponse(description = "Successful operation", responseCode = "200"),
        ApiResponse(description = "Files not found", responseCode = "400", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @GetMapping("/storageinfo/{storageId}", produces = ["application/json"])
    fun getStorageInfoByStorageId(@PathVariable("storageId") storageId: String, request: HttpServletRequest): ResponseEntity<StorageInfoResponse> {
        logger.info("Retrieving Storage Information for $storageId anonymously")
        val storageInfo = storageService.getStorageInfoFromBucket(StorageUtils.ANONYMOUS_BUCKET, storageId)
        val response = StorageInfoResponse(storageId, storageInfo.filenames, storageInfo.numOfDownloadsLeft, storageInfo.expiryDatetime)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @Operation(summary = "Download files using storageId anonymously")
    @ApiResponses(value = [
        ApiResponse(description = "Successful operation", responseCode = "200"),
        ApiResponse(description = "Files not found", responseCode = "400", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))])
    ])
    @GetMapping("/download/{storageId}")
    fun downloadFiles(@PathVariable("storageId") storageId: String, response: HttpServletResponse) {
        logger.info("Downloading Storage Information for $storageId} anonymously")
        storageService.downloadFilesFromBucket(StorageUtils.ANONYMOUS_BUCKET, storageId, response)
    }
}