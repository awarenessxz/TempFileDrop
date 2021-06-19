package com.tempstorage.storagesvc.service.storageinfo

import org.springframework.data.mongodb.repository.MongoRepository
import java.time.ZonedDateTime

interface StorageInfoRepository: MongoRepository<StorageInfo, String> {
    fun deleteByIdIn(storageIds: List<String>)
    fun findByBucketName(bucket: String): List<StorageInfo>
    fun findByExpiryDatetimeBeforeOrNumOfDownloadsLeftLessThan(datetime: ZonedDateTime, count: Int): List<StorageInfo>
}