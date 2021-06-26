package com.tempstorage.storagesvc.service.storageinfo

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime

@Document(collection="storage_info")
data class StorageInfo(
        val bucket: String,                     // bucket_name
        val storagePath: String,                // the directory
        val filenames: String,                  // files uploaded delimited by comma
        val numOfDownloadsLeft: Int,            // number of downloads left
        val expiryDatetime: ZonedDateTime,      // expiry date time
        val allowAnonymousDownload: Boolean,    // allow anonymous download
        @Id val id: ObjectId? = ObjectId.get()  // mongoDB ID (Storage ID)
)