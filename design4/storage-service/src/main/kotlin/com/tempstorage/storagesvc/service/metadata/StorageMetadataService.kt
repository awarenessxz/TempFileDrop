package com.tempstorage.storagesvc.service.metadata

interface StorageMetadataService {
    fun saveStorageMetadata(storageMetadata: StorageMetadata)
    fun deleteStorageMetadataByObjectName(objectName: String)
    fun getBuckets(): List<String>
    fun getAllStorageMetadata(): List<StorageMetadata>
    fun getAllStorageMetadataInBucket(bucket: String): List<StorageMetadata>
    fun getStorageMetadataByObjectName(objectName: String): StorageMetadata?
    fun getExpiredStorageMetadataList(): List<StorageMetadata>
    fun reduceDownloadCountByObjectName(objectName: String)
}