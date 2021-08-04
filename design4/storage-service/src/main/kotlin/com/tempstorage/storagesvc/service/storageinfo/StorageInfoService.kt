package com.tempstorage.storagesvc.service.storageinfo

interface StorageInfoService {
    fun addStorageInfo(storageInfo: StorageInfo)
    fun deleteStorageInfoById(storageId: String)
    fun deleteStorageInfoByIdBulk(storageIds: List<String>)
    fun getAllStorageInfoInBucket(bucket: String): List<StorageInfo>
    fun getStorageInfoById(storageId: String): StorageInfo?
    fun getStorageInfoByPath(storagePath: String): StorageInfo?
    fun getBulkStorageInfoById(storageIds: List<String>): List<StorageInfo>
    fun getExpiredStorageInfoList(): List<StorageInfo>
    fun reduceDownloadCountById(storageId: String)
    fun getBuckets(): List<String>
    fun getAllStorageInfo(): List<StorageInfo>
}