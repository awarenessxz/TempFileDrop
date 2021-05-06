package com.tempfiledrop.webserver.service.useruploads

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserUploadRecordServiceImpl(
        private val repository: UserUploadRecordRepository
): UserUploadRecordService {
    override fun addUserUploadRecord(record: UserUploadRecord) {
        repository.save(record)
    }

    override fun getUserUploadRecordById(id: String): UserUploadRecord? {
        return repository.findByIdOrNull(id)
    }

    override fun listUserUploadRecord(folderName: String): List<UserUploadRecord> {
        return repository.findByFolder(folderName)
    }

    override fun deleteUserUploadRecord(id: String) {
        repository.deleteById(id)
    }
}