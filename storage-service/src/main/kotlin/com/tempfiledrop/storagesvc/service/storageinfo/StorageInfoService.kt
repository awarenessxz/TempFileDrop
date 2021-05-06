package com.tempfiledrop.storagesvc.service.storageinfo

interface StorageInfoService {
    fun addStorageInfo(filepathList: List<String>, storageInfo: StorageInfo): String
    fun deleteStorageInfoById(storageId: String)
    fun getStorageInfosByStorageId(storageId: String): List<StorageInfo>
}