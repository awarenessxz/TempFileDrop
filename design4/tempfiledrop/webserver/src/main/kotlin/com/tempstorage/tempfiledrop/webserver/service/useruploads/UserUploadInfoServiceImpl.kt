package com.tempstorage.tempfiledrop.webserver.service.useruploads

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserUploadInfoServiceImpl(
        private val repository: UserUploadInfoRepository
): UserUploadInfoService {
    override fun addUploadedFilesRecord(record: UserUploadInfo) {
        val prevRecord = repository.findByObjectName(record.objectName)
        if (prevRecord != null) {
            record.id = prevRecord.id
        }
        repository.save(record)
    }

    override fun getUploadedFilesRecordById(id: String): UserUploadInfo? {
        return repository.findByIdOrNull(id)
    }

    override fun getUploadedFilesRecordsForUser(user: String): List<UserUploadInfo> {
        return repository.findByUser(user)
    }

    override fun deleteUploadedFilesRecordById(id: String) {
        repository.deleteById(id)
    }

    override fun deleteUploadedFilesRecordByObjectName(objectName: String) {
        repository.deleteByObjectName(objectName)
    }
}