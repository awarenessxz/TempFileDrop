package com.tempfiledrop.webserver.service.storagesvcclient

import com.tempfiledrop.webserver.config.ServerProperties
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile

@Service
class StorageSvcClientImpl(
        private val serverProperties: ServerProperties
): StorageSvcClient {
    companion object {
        private val logger = LoggerFactory.getLogger(StorageSvcClientImpl::class.java)
    }

    override fun uploadToStorageSvc(files: List<MultipartFile>, storageRequest: StorageRequest): ResponseEntity<StorageResponse> {
        logger.info("Forwarding Request to Storage Service...")

        // craft header
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        // craft body
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        files.forEach { body.add("files", it.resource) }
        body.add("metadata", storageRequest)

        // craft request
        val requestEntity: HttpEntity<MultiValueMap<String, Any>> = HttpEntity(body, headers)
        val storageServiceUrl = "${serverProperties.storageServiceUrl}/upload"
        val restTemplate = RestTemplate()
        val response = restTemplate.postForEntity(storageServiceUrl, requestEntity, StorageResponse::class.java)

        logger.info("Response From Storage Service Received: $response")
        return response
    }

    override fun deleteFilesInFolder(bucket: String, storageId: String) {
        logger.info("Forwarding Delete Request to Storage Service...")
        val storageServiceUrl = "${serverProperties.storageServiceUrl}/$bucket/$storageId"
        val restTemplate = RestTemplate()
        restTemplate.delete(storageServiceUrl)
    }
}