package com.tempfiledrop.storagesvc.service.objectstorage

import com.tempfiledrop.storagesvc.model.StorageInfo
import org.springframework.web.multipart.MultipartFile

interface ObjectStorageService {
    fun minioUpload(files: List<MultipartFile>, storageInfo: StorageInfo)
}