package com.tempstorage.storagesvc.service.storageinfo

import com.tempstorage.storagesvc.exception.ApiException
import com.tempstorage.storagesvc.exception.ErrorCode
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class StorageInfoServiceImpl(
        private val repository: StorageInfoRepository,
        private val mongoTemplate: MongoTemplate
): StorageInfoService {
    override fun addStorageInfo(storageInfo: StorageInfo) {
        repository.save(storageInfo)
    }

    override fun deleteStorageInfoById(storageId: String) {
        repository.deleteById(storageId)
    }

    override fun deleteStorageInfoByIdBulk(storageIds: List<String>) {
        repository.deleteByIdIn(storageIds)
    }

    override fun getStorageInfosInBucket(bucket: String): List<StorageInfo> {
        return repository.findByBucket(bucket)
    }

    override fun getAllStorageInfo(): List<StorageInfo> {
        return repository.findAll()
    }

    override fun getStorageInfoById(storageId: String): StorageInfo? {
        return repository.findByIdOrNull(storageId)
    }

    override fun getStorageInfoByPath(storagePath: String): StorageInfo? {
        return repository.findByStorageFullPath(storagePath)
    }

    override fun getBulkStorageInfoById(storageIds: List<String>): List<StorageInfo> {
        val results = repository.findAllById(storageIds)
        return results.map { it }
    }

    override fun getExpiredStorageInfoList(): List<StorageInfo> {
        return repository.findByExpiryDatetimeBeforeOrNumOfDownloadsLeftLessThan(ZonedDateTime.now(), 1)
    }

    override fun reduceDownloadCountById(storageId: String) {
        val storageInfo = repository.findByIdOrNull(storageId) ?: throw ApiException("Record not found!", ErrorCode.SERVER_ERROR, HttpStatus.BAD_REQUEST)
        val newStorageInfo = StorageInfo(
                storageInfo.id,
                storageInfo.bucket,
                storageInfo.storagePath,
                storageInfo.originalFilename,
                storageInfo.fileContentType,
                storageInfo.fileLength,
                storageInfo.numOfDownloadsLeft,
                storageInfo.expiryDatetime,
                storageInfo.allowAnonymousDownload
        )
        repository.save(newStorageInfo)
    }

    override fun getBuckets(): List<String> {
        return mongoTemplate.query(StorageInfo::class.java)
                .distinct("bucket")
                .`as`(String::class.java)
                .all()
    }
}