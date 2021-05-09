package com.tempfiledrop.storagesvc.service.storageinfo

interface StorageInfoService {
    fun addStorageInfo(storageInfo: StorageInfo)
    fun deleteStorageInfoById(storageId: String)
    fun getStorageInfoById(storageId: String): StorageInfo?
    fun reduceDownloadCountById(storageId: String)
}