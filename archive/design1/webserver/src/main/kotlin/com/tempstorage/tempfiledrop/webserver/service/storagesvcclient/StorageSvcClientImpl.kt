package com.tempstorage.tempfiledrop.webserver.service.storagesvcclient

import com.tempstorage.tempfiledrop.webserver.config.StorageSvcClientProperties
import com.tempstorage.tempfiledrop.webserver.exception.ApiException
import com.tempstorage.tempfiledrop.webserver.util.StreamResponseExtractor
import com.tempstorage.tempfiledrop.webserver.util.StreamResourceReader
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse

@Service
class StorageSvcClientImpl(
        private val props: StorageSvcClientProperties,
        private val storageSvcRestTemplate: RestTemplate
): StorageSvcClient {
    companion object {
        private val logger = LoggerFactory.getLogger(StorageSvcClientImpl::class.java)
    }

    @Throws(ApiException::class)
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
        val storageServiceUrl = "${props.storageServiceUrl}/storagesvc/upload"
        val response = storageSvcRestTemplate.postForEntity(storageServiceUrl, requestEntity, StorageUploadResponse::class.java)

        logger.info("Response From Storage Service Received: $response")
        return response
    }

    @Throws(ApiException::class)
    override fun deleteFilesInFolder(bucket: String, storageId: String) {
        logger.info("Forwarding Delete Request to Storage Service...")
        val storageServiceUrl = "${props.storageServiceUrl}/storagesvc/$bucket/$storageId"
        storageSvcRestTemplate.delete(storageServiceUrl)
    }

    @Throws(ApiException::class)
    override fun getStorageInfoByStorageId(bucket: String, storageId: String): ResponseEntity<StorageInfoResponse> {
        logger.info("Forwarding GET Request to Storage Service...")
        val storageServiceUrl = "${props.storageServiceUrl}/storagesvc/storageinfo/$bucket/$storageId"
        val response = storageSvcRestTemplate.getForEntity(storageServiceUrl, StorageInfoResponse::class.java)
        logger.info("Response From Storage Service Received: $response")
        return response
    }

    @Throws(ApiException::class)
    override fun downloadFromStorageSvc(bucket: String, storageId: String, response: HttpServletResponse) {
        logger.info("Forwarding Download Request to Storage Service...")
        val storageServiceUrl = "${props.storageServiceUrl}/storagesvc/download/$bucket/$storageId"
        storageSvcRestTemplate.execute(storageServiceUrl, HttpMethod.GET, null, StreamResponseExtractor(StreamResourceReader(response)))
    }
}