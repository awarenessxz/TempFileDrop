package com.tempfiledrop.storagesvc.service.storageinfo

import org.springframework.data.mongodb.repository.MongoRepository

interface StorageInfoRepository: MongoRepository<StorageInfo, String> {
    fun deleteByStorageId(storageId: String)
    fun findByStorageId(storageId: String): List<StorageInfo>
}