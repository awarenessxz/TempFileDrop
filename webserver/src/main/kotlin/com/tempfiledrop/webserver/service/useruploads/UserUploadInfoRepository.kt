package com.tempfiledrop.webserver.service.useruploads

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserUploadInfoRepository: MongoRepository<UserUploadInfo, String> {
    fun findByUser(user: String): List<UserUploadInfo>
    fun findByStorageId(storageId: String): UserUploadInfo?
}