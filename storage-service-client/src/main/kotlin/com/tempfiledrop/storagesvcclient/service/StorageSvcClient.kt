package com.tempfiledrop.storagesvcclient.service

import com.tempfiledrop.storagesvcclient.model.StorageInfoResponse
import com.tempfiledrop.storagesvcclient.model.StorageUploadRequest
import com.tempfiledrop.storagesvcclient.model.StorageUploadResponse
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.multipart.MultipartFile

interface StorageSvcClient {
    fun uploadToStorageSvc(files: List<MultipartFile>, storageRequest: StorageUploadRequest): ResponseEntity<StorageUploadResponse>
    fun deleteFilesInFolder(bucket: String, storageId: String)
    fun getStorageInfoByStorageId(bucket: String, storageId: String): ResponseEntity<StorageInfoResponse>
    fun downloadFromStorageSvc(bucket: String, storageId: String): ResponseEntity<Resource>
}