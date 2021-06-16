package com.tempfiledrop.storagesvc.controller

import com.tempfiledrop.storagesvc.config.StorageSvcProperties
import com.tempfiledrop.storagesvc.controller.storage.StorageUploadResponse
import com.tempfiledrop.storagesvc.exception.ErrorResponse
import com.tempfiledrop.storagesvc.service.storage.StorageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MaxUploadSizeExceededException
import javax.servlet.http.HttpServletRequest
import kotlin.io.path.ExperimentalPathApi

@RestController
@RequestMapping("/api/storagesvc/anonymous")
@ConditionalOnProperty(prefix = "storagesvc.anonymous-upload", name = ["enable"], havingValue = "true")
class AnonymousController(
        private val storageService: StorageService,
        private val storagesvcProps: StorageSvcProperties
) {
    companion object {
        private val logger = LoggerFactory.getLogger(AnonymousController::class.java)
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
}
