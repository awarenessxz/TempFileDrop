package com.tempfiledrop.webserver.service.useruploads

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserUploadRecordRepository: MongoRepository<UserUploadRecord, String> {
    fun findByFolder(folderName: String): List<UserUploadRecord>
}