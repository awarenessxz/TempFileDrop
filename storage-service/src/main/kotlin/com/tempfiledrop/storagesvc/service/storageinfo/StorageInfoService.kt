package com.tempfiledrop.storagesvc.service.storageinfo

interface StorageInfoService {
    fun addStorageInfo(storageInfo: StorageInfo)
    fun deleteStorageInfoById(storageId: String)
    fun getStorageInfosInBucket(bucket: String): List<StorageInfo>
    fun getStorageInfoById(storageId: String): StorageInfo?
    fun getBulkStorageInfoById(storageIds: List<String>): List<StorageInfo>
    fun reduceDownloadCountById(storageId: String)
}