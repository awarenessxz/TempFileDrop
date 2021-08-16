package com.tempstorage.storagesvc.service.metadata

import org.springframework.data.mongodb.repository.MongoRepository
import java.time.ZonedDateTime

interface StorageMetadataRepository: MongoRepository<StorageMetadata, String> {
    fun deleteByObjectName(objectName: String)
    fun findByBucket(bucket: String): List<StorageMetadata>
    fun findByExpiryDatetimeBeforeOrNumOfDownloadsLeftLessThan(datetime: ZonedDateTime, count: Int): List<StorageMetadata>
    fun findByObjectName(objectName: String): StorageMetadata?
}