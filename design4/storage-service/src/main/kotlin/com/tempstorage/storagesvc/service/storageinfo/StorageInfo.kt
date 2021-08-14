package com.tempstorage.storagesvc.service.storageinfo

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime
import java.nio.file.Paths

@Document(collection="storage_info")
class StorageInfo(
        val bucket: String,                     // bucket_name
        val objectName: String,                 // s3 object name
        val fileContentType: String,            // file content type
        val fileSize: Long,                     // file size
        val numOfDownloadsLeft: Int,            // number of downloads left
        val expiryDatetime: ZonedDateTime,      // expiry date time
        val allowAnonymousDownload: Boolean,    // allow anonymous download
        val status: StorageStatus,              // Uploaded / Pending / Deleted
        @Id val id: ObjectId = ObjectId.get()   // mongoDB ID (Storage ID)
) {
    val storageFullPath = Paths.get(bucket).resolve(objectName).toString() // full storage path = bucket/storagePath/originalFilename
    val storagePath = objectName.substringBeforeLast("/")
    val originalFilename = objectName.substringAfterLast("/")
}
