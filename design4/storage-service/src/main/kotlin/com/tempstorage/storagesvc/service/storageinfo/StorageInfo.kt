package com.tempstorage.storagesvc.service.storageinfo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime

@Document(collection="storage_info")
data class StorageInfo(
        @Id val id: String,                     // mongoDB ID (Storage ID)
        val bucket: String,                     // bucket_name
        val storagePath: String,                // the directory
        val filenames: String,                  // files uploaded delimited by comma
        val numOfDownloadsLeft: Int,            // number of downloads left
        val expiryDatetime: ZonedDateTime,      // expiry date time
        val allowAnonymousDownload: Boolean     // allow anonymous download
)