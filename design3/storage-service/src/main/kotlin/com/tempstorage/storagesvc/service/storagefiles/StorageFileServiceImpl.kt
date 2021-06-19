package com.tempstorage.storagesvc.service.storagefiles

import org.springframework.stereotype.Service

@Service
class StorageFileServiceImpl(
        private val repository: StorageFileRepository
): StorageFileService {
    override fun saveFilesInfo(storageId: String, files: List<StorageFile>) {
        files.forEach { repository.save(StorageFile(it.bucket, it.storagePath, it.originalFilename, it.filename, it.fileContentType, it.fileLength, storageId)) }
    }

    override fun deleteFilesInfo(storageId: String) {
        repository.deleteByStorageId(storageId)
    }

    override fun deleteFilesInfoBulk(storageIds: List<String>) {
        repository.deleteByStorageIdIn(storageIds)
    }

    override fun getStorageFilesInfoByStorageId(storageId: String): List<StorageFile> {
        return repository.findByStorageId(storageId)
    }

    override fun getStorageFilesInfoByStorageIdBulk(storageIds: List<String>): List<StorageFile> {
        return repository.findByStorageIdIn(storageIds)
    }
}