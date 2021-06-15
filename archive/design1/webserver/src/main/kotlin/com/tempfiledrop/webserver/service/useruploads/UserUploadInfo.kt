package com.tempfiledrop.webserver.service.useruploads

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="users_upload_info")
data class UserUploadInfo (
    @Id
    val id: String?,                    // mongoDB ID
    val user: String,                   // user
    val storageId: String,              // id of stored location in the storage service
)