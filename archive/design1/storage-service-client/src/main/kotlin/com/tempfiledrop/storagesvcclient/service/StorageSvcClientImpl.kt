package com.tempfiledrop.storagesvcclient.service

import com.tempfiledrop.storagesvcclient.config.ClientProperties
import com.tempfiledrop.storagesvcclient.model.StorageInfoResponse
import com.tempfiledrop.storagesvcclient.model.StorageUploadRequest
import com.tempfiledrop.storagesvcclient.model.StorageUploadResponse
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile

@Service
class StorageSvcClientImpl(
        private val clientProperties: ClientProperties
): StorageSvcClient {
    companion object {
        private val logger = LoggerFactory.getLogger(StorageSvcClientImpl::class.java)
    }

    override fun uploadToStorageSvc(files: List<MultipartFile>, storageRequest: StorageUploadRequest): ResponseEntity<StorageUploadResponse> {
        logger.info("Forwarding Upload Request to Storage Service...")

        // craft header
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        // craft body
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        files.forEach { body.add("files", it.resource) }
        body.add("metadata", storageRequest)

        // craft request
        val requestEntity: HttpEntity<MultiValueMap<String, Any>> = HttpEntity(body, headers)
        val storageServiceUrl = "${clientProperties.storageServiceUrl}/storagesvc/upload"
        val restTemplate = RestTemplate()
        val response = restTemplate.postForEntity(storageServiceUrl, requestEntity, StorageUploadResponse::class.java)

        logger.info("Response From Storage Service Received: $response")
        return response
    }

    override fun deleteFilesInFolder(bucket: String, storageId: String) {
        logger.info("Forwarding Delete Request to Storage Service...")
        val storageServiceUrl = "${clientProperties.storageServiceUrl}/storagesvc/$bucket/$storageId"
        val restTemplate = RestTemplate()
        restTemplate.delete(storageServiceUrl)
    }

    override fun getStorageInfoByStorageId(bucket: String, storageId: String): ResponseEntity<StorageInfoResponse> {
        logger.info("Forwarding GET Request to Storage Service...")
        val storageServiceUrl = "${clientProperties.storageServiceUrl}/storagesvc/storageinfo/$bucket/$storageId"
        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity(storageServiceUrl, StorageInfoResponse::class.java)
        logger.info("Response From Storage Service Received: $response")
        return response
    }

    override fun downloadFromStorageSvc(bucket: String, storageId: String): ResponseEntity<Resource> {
        logger.info("Forwarding Download Request to Storage Service...")
        val storageServiceUrl = "${clientProperties.storageServiceUrl}/storagesvc/download/$bucket/$storageId"
        val restTemplate = RestTemplate()
        return restTemplate.exchange(storageServiceUrl, HttpMethod.GET, null, Resource::class.java)
    }
}