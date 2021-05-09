package com.tempfiledrop.storagesvc.service.storageinfo

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class StorageInfoServiceImpl(
        private val repository: StorageInfoRepository
): StorageInfoService {
    override fun addStorageInfo(files: List<MultipartFile>, storageInfo: StorageInfo): String {
        val storageId = UUID.randomUUID().toString()
        files.forEach {
            val fileStorageInfo = StorageInfo(
                    storageInfo.bucketName,
                    storageInfo.storagePath,
                    it.originalFilename.toString(),
                    it.contentType,
                    it.size,
                    storageId
            )
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