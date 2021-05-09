package com.tempfiledrop.storagesvc.service.storagefiles

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class StorageFileServiceImpl(
        private val repository: StorageFileRepository
): StorageFileService {
    override fun saveFilesInfo(bucket: String, storagePath: String, storageId: String, files: List<MultipartFile>) {
        files.forEach { repository.save(StorageFile(bucket, storagePath, it.originalFilename.toString(), it.contentType, it.size, storageId)) }
    }

    override fun deleteFilesInfo(storageId: String) {
        repository.deleteByStorageId(storageId)
    }

    override fun getStorageFilesInfoByStorageId(storageId: String): List<StorageFile> {
        return repository.findByStorageId(storageId)
    }
}