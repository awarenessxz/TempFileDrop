package com.tempfiledrop.storagesvc.service.storageinfo

import org.springframework.stereotype.Service
import java.util.*

@Service
class StorageInfoServiceImpl(
        private val repository: StorageInfoRepository
): StorageInfoService {
    override fun addStorageInfo(filenamesList: List<String>, storageInfo: StorageInfo): String {
        val storageId = UUID.randomUUID().toString()
        filenamesList.forEach {
            val fileStorageInfo = StorageInfo(storageInfo.bucketName, storageInfo.storagePath, it, storageId)
            repository.save(fileStorageInfo)
        }
        return storageId
    }

    override fun deleteStorageInfoById(storageId: String) {
        repository.deleteByStorageId(storageId)
    }

    override fun getStorageInfosByStorageId(storageId: String): List<StorageInfo> {
        return repository.findByStorageId(storageId)
    }
}