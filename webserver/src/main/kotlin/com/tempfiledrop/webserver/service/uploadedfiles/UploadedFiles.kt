package com.tempfiledrop.webserver.service.uploadedfiles

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime

@Document(collection="uploaded_files")
data class UploadedFiles (
    @Id
    val id: String?,                    // mongoDB ID
    val user: String,                   // user
    val filenames: String,              /// files uploaded delimited by comma
    val numOfDownloadsLeft: Int,        // number of downloads left
    val expiryDatetime: ZonedDateTime,  // expiry date time
    val storageId: String,              // id of stored location in the storage service
    val downloadLink: String            // download link
)