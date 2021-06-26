package com.tempstorage.storagesvc.controller.download

import com.tempstorage.storagesvc.exception.ErrorResponse
import com.tempstorage.storagesvc.service.download.DownloadTokenService
import com.tempstorage.storagesvc.service.event.EventType
import com.tempstorage.storagesvc.service.event.RabbitMQProducer
import com.tempstorage.storagesvc.service.storage.StorageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api/storagesvc/download")
class DownloadController(
        private val storageService: StorageService,
        private val downloadTokenService: DownloadTokenService,
        private val producer: RabbitMQProducer
) {
    companion object {
        private val logger = LoggerFactory.getLogger(DownloadController::class.java)
    }

    @Operation(summary = "Get Download Information with download key provided in download endpoint")
    @ApiResponses(value = [
        ApiResponse(description = "Successful operation", responseCode = "200"),
        ApiResponse(description = "Files not found", responseCode = "400", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ])
    @GetMapping("/temporarykey/{storageId}")
    fun getDownloadLink(@PathVariable("storageId") storageId: String): ResponseEntity<DownloadResponse> {
        logger.info("Generating Download Link for $storageId....")
        val storageInfo = storageService.getAndValidateStorageInfo(storageId)
        val requiresAuth = !storageInfo.allowAnonymousDownload
        val token = downloadTokenService.generateDownloadToken(storageId)
        val endpoint = if (requiresAuth) "/api/storagesvc/download/secure/${token.downloadKey}" else "/api/storagesvc/download/${token.downloadKey}"
        val response = DownloadResponse(endpoint, token.expiryDatetime, storageInfo.numOfDownloadsLeft, storageInfo.expiryDatetime, requiresAuth)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @Operation(summary = "Download files using temporary download key")
    @ApiResponses(value = [
        ApiResponse(description = "Successful operation", responseCode = "200"),
        ApiResponse(description = "Files not found", responseCode = "400", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(description = "Not Authorized", responseCode = "401")
    ])
    @GetMapping("/{downloadKey}")
    fun downloadFiles(
            @PathVariable("downloadKey") downloadKey: String,
            @RequestParam(required = false, defaultValue = "") eventData: String,
            @RequestParam(required = false, defaultValue = "") eventRoutingKey: String,
            response: HttpServletResponse
    ) {
        logger.info("Downloading files from $downloadKey")
        val token = downloadTokenService.checkIfTokenExpired(downloadKey)
        val storageInfo = storageService.downloadFilesFromBucket(token!!.storageId, response) // download files
        if (eventRoutingKey.isNotEmpty()) {
            producer.sendEventwithHeader(EventType.FILES_DOWNLOADED, storageInfo, eventData, eventRoutingKey, false) // send an event
        }
    }

    @Operation(summary = "Download files with authorization header using temporary download key", security = [SecurityRequirement(name = "bearer-token")])
    @ApiResponses(value = [
        ApiResponse(description = "Successful operation", responseCode = "200"),
        ApiResponse(description = "Files not found", responseCode = "400", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ])
    @GetMapping("/secure/{downloadKey}")
    fun downloadFilesAuthenticated(
            @PathVariable("downloadKey") downloadKey: String,
            @RequestParam(required = false, defaultValue = "") eventData: String,
            @RequestParam(required = false, defaultValue = "") eventRoutingKey: String,
            response: HttpServletResponse
    ) {
        logger.info("Downloading files from $downloadKey with authorization header")
        val token = downloadTokenService.checkIfTokenExpired(downloadKey)
        val storageInfo = storageService.downloadFilesFromBucket(token!!.storageId, response, true) // download files
        if (eventRoutingKey.isNotEmpty()) {
            producer.sendEventwithHeader(EventType.FILES_DOWNLOADED, storageInfo, eventData, eventRoutingKey, false) // send an event
        }
    }

    @Operation(summary = "Download files with authorization header, bypassing download key usage", security = [SecurityRequirement(name = "bearer-token")])
    @ApiResponses(value = [
        ApiResponse(description = "Successful operation", responseCode = "200"),
        ApiResponse(description = "Files not found", responseCode = "400", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ])
    @GetMapping("/secure/{bucket}/{storageId}")
    fun downloadFilesAuthenticatedDirectly(
            @PathVariable("bucket") bucket: String,
            @PathVariable("storageId") storageId: String,
            @RequestParam(required = false, defaultValue = "") eventData: String,
            @RequestParam(required = false, defaultValue = "") eventRoutingKey: String,
            response: HttpServletResponse
    ) {
        logger.info("Downloading files from $storageId with authorization header directly without using download key")
        val storageInfo = storageService.downloadFilesFromBucket(storageId, response, true, bucket) // download files
        if (eventRoutingKey.isNotEmpty()) {
            producer.sendEventwithHeader(EventType.FILES_DOWNLOADED, storageInfo, eventData, eventRoutingKey, false) // send an event
        }
    }
}