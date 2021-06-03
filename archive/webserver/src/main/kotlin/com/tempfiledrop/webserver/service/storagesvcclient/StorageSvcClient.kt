package com.tempfiledrop.webserver.service.storagesvcclient

import org.springframework.http.ResponseEntity
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse

interface StorageSvcClient {
    fun uploadToStorageSvc(files: List<MultipartFile>, storageRequest: StorageUploadRequest): ResponseEntity<StorageUploadResponse>
    fun deleteFilesInFolder(bucket: String, storageId: String)
    fun getStorageInfoByStorageId(bucket: String, storageId: String): ResponseEntity<StorageInfoResponse>
    fun downloadFromStorageSvc(bucket: String, storageId: String, response: HttpServletResponse)
}