package com.tempfiledrop.console.webserver.service.useruploads

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserUploadInfoServiceImpl(
        private val repository: UserUploadInfoRepository
): UserUploadInfoService {
    override fun addUploadedFilesRecord(record: UserUploadInfo) {
        repository.save(record)
    }

    override fun getUploadedFilesRecordById(id: String): UserUploadInfo? {
        return repository.findByIdOrNull(id)
    }

    override fun getUploadedFilesRecordByStorageId(storageId: String): UserUploadInfo? {
        return repository.findByStorageId(storageId)
    }

    override fun getUploadedFilesRecordsForUser(user: String): List<UserUploadInfo> {
        return repository.findByUser(user)
    }

    override fun deleteUploadedFilesRecordById(id: String) {
        repository.deleteById(id)
    }

    override fun deleteUploadedFilesRecordByStorageId(storageId: String) {
        repository.deleteByStorageId(storageId)
    }
}