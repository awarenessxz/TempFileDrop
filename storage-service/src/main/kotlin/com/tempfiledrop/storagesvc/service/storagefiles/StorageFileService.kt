package com.tempfiledrop.storagesvc.service.storagefiles

import org.springframework.web.multipart.MultipartFile

interface StorageFileService {
    fun saveFilesInfo(bucket: String, storagePath: String, storageId: String, files: List<MultipartFile>)
    fun deleteFilesInfo(storageId: String)
    fun getStorageFilesInfoByStorageId(storageId: String): List<StorageFile>
}