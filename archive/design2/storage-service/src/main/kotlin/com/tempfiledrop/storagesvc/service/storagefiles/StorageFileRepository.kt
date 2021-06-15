package com.tempfiledrop.storagesvc.service.storagefiles

import org.springframework.data.mongodb.repository.MongoRepository

interface StorageFileRepository: MongoRepository<StorageFile, String> {
    fun deleteByStorageId(storageId: String)
    fun deleteByStorageIdIn(storageIds: List<String>)
    fun findByStorageId(storageId: String): List<StorageFile>
    fun findByStorageIdIn(storageIds: List<String>): List<StorageFile>
}