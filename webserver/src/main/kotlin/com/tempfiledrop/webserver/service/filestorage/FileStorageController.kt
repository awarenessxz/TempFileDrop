package com.tempfiledrop.webserver.service.filestorage

import com.tempfiledrop.webserver.config.ServerProperties
import com.tempfiledrop.webserver.exception.ApiException
import com.tempfiledrop.webserver.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/files")
class FileStorageController(
        private val serverProperties: ServerProperties
) {
    companion object {
        private val logger = LoggerFactory.getLogger(FileStorageController::class.java)
    }

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
            @RequestPart("files", required = true) files: List<MultipartFile>,
            @RequestPart("metadata", required = true) metadata: FileUploadInfoRequest
    ): ResponseEntity<FileStorageResponse> {
        var message = ""
        val filesStored = files.joinToString(", ") { it.originalFilename.toString() }
        val folderName = if (metadata.username.trim().isEmpty()) "anonymous" else metadata.username

        // Forward to Storage Service to store file
        try {
            // craft header
            val headers = HttpHeaders()
            headers.contentType = MediaType.MULTIPART_FORM_DATA
            // craft body
            val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
            files.forEach { body.add("files", it.resource) }
            body.add("metadata", StorageRequest("s3://${serverProperties.bucketName}/$folderName"))
            // craft request
            val requestEntity: HttpEntity<MultiValueMap<String, Any>> = HttpEntity(body, headers)
            // craft rest template
            val storageServiceUrl = "${serverProperties.storageServiceUrl}/upload"
            val restTemplate = RestTemplate()
            val response = restTemplate.postForEntity(storageServiceUrl, requestEntity, String::class.java)

            // Need to store inside database
            logger.info(response.toString())

            // process response & return results
            return if (response.statusCode.is2xxSuccessful) {
                message = "Uploaded Successfully: $filesStored"
                ResponseEntity(FileStorageResponse(message), HttpStatus.OK)
            } else {
                message = "Could not upload the files! - $filesStored"
                ResponseEntity(FileStorageResponse(message), HttpStatus.INTERNAL_SERVER_ERROR)
            }

        } catch (e: HttpStatusCodeException) {
            throw ApiException("Uploading Failed!", ErrorCode.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}