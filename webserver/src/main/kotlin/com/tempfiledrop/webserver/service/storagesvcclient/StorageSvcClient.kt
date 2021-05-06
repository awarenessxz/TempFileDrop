package com.tempfiledrop.webserver.service.storagesvcclient

import org.springframework.http.ResponseEntity
import org.springframework.web.multipart.MultipartFile

interface StorageSvcClient {
    fun uploadToStorageSvc(files: List<MultipartFile>, storageRequest: StorageRequest): ResponseEntity<StorageResponse>
    fun deleteFilesInFolder(bucket: String, storageId: String)
}