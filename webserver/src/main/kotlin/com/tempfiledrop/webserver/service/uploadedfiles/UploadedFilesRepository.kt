package com.tempfiledrop.webserver.service.uploadedfiles

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UploadedFilesRepository: MongoRepository<UploadedFiles, String> {
    fun findByUser(user: String): List<UploadedFiles>
    fun findByStorageId(storageId: String): UploadedFiles?
}