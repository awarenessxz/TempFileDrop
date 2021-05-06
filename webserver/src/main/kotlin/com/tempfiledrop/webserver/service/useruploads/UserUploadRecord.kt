package com.tempfiledrop.webserver.service.useruploads

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="users_uploads")
data class UserUploadRecord (
    @Id
    val id: String?,                  // mongoDB ID
    val folder: String,               // sub folder in bucket
    val uploadedFiles: String,        // list of all files uploaded
    val storageId: String,            // id of stored location in the storage service
    val downloadLink: String          // url that user see to download
)