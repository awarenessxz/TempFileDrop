package com.tempstorage.storagesvc.service.storageinfo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime
import java.nio.file.Paths

@Document(collection="storage_info")
data class StorageInfo(
        @Id val id: String,                     // mongoDB ID (Storage ID)
        val bucket: String,                     // bucket_name
        val storagePath: String,                // the directory
        val originalFilename: String,           // original file name
        val fileContentType: String?,           // content type
        val fileLength: Long,                   // file size
        val numOfDownloadsLeft: Int,            // number of downloads left
        val expiryDatetime: ZonedDateTime,      // expiry date time
        val allowAnonymousDownload: Boolean,    // allow anonymous download
        var storageFullPath: String? = "",      // full storage path = bucket/storagePath/originalFilename
) {
    init {
        storageFullPath = Paths.get(bucket).resolve(storagePath).resolve(originalFilename).toString()
    }

    fun getObjectName(): String {
        return Paths.get(storagePath).resolve(originalFilename).toString()
    }
}
