package com.tempfiledrop.storagesvc.service.storageinfo

import org.springframework.web.multipart.MultipartFile

interface StorageInfoService {
    fun addStorageInfo(files: List<MultipartFile>, storageInfo: StorageInfo): String
    fun deleteStorageInfoById(storageId: String)
    fun getStorageInfosByStorageId(storageId: String): List<StorageInfo>
}