package com.tempstorage.storagesvc.service.metadata

import com.tempstorage.storagesvc.exception.ApiException
import com.tempstorage.storagesvc.exception.ErrorCode
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class StorageMetadataServiceImpl(
        private val repository: StorageMetadataRepository,
        private val mongoTemplate: MongoTemplate
): StorageMetadataService {
    override fun saveStorageMetadata(storageMetadata: StorageMetadata) {
        val prevMetadata = repository.findByObjectName(storageMetadata.objectName)
        if (prevMetadata != null) {
            storageMetadata.id = prevMetadata.id
        }
        repository.save(storageMetadata)
    }

    override fun deleteStorageMetadataByObjectName(objectName: String) {
        repository.deleteByObjectName(objectName)
    }

    override fun getAllStorageMetadataInBucket(bucket: String): List<StorageMetadata> {
        return repository.findByBucket(bucket)
    }

    override fun getAllStorageMetadata(): List<StorageMetadata> {
        return repository.findAll()
    }

    override fun getStorageMetadataByObjectName(objectName: String): StorageMetadata? {
        return repository.findByObjectName(objectName)
    }

    override fun getExpiredStorageMetadataList(): List<StorageMetadata> {
        return repository.findByExpiryDatetimeBeforeOrNumOfDownloadsLeftLessThan(ZonedDateTime.now(), 1)
    }

    override fun reduceDownloadCountByObjectName(objectName: String) {
        val storageInfo = repository.findByObjectName(objectName) ?: throw ApiException("Record not found!", ErrorCode.SERVER_ERROR, HttpStatus.BAD_REQUEST)
        storageInfo.reduceDownloadCount(1)
        repository.save(storageInfo)
    }

    override fun getBuckets(): List<String> {
        return mongoTemplate.query(StorageMetadata::class.java)
                .distinct("bucket")
                .`as`(String::class.java)
                .all()
    }
}