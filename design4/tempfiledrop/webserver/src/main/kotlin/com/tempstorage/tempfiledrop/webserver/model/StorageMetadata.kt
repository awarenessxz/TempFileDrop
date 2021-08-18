package com.tempstorage.tempfiledrop.webserver.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import java.nio.file.Paths
import java.time.ZonedDateTime

class StorageMetadata(
        val bucket: String,                                 // bucket_name
        val objectName: String,                             // s3 object name
        val fileContentType: String,                        // file content type
        val fileSize: Long,                                 // file size
        var numOfDownloadsLeft: Int,                        // number of downloads left
        val expiryDatetime: ZonedDateTime?,                 // expiry date time
        val allowAnonymousDownload: Boolean,                // allow anonymous download
        @Id var id: ObjectId = ObjectId.get()               // mongoDB ID (Storage ID)
) {
    companion object {
        const val EXPIRY_PERIOD = "X-Amz-Meta-Storage-Expiry"
        const val MAX_DOWNLOAD_COUNT = "X-Amz-Meta-Storage-Max-Download-Count"
    }

    fun getStorageFullPath(): String {
        return Paths.get(bucket).resolve(objectName).toString() // full storage path = bucket/storagePath/originalFilename
    }

    fun getStoragePath(): String {
        return objectName.substringBeforeLast("/")
    }

    fun getOriginalFilename(): String {
        return objectName.substringAfterLast("/")
    }

    fun reduceDownloadCount(amount: Int) {
        this.numOfDownloadsLeft -= amount
    }

    override fun toString(): String {
        return "StorageMetadata($id, Bucket=$bucket, ObjectName=$objectName, ContentType=$fileContentType, FileSize=$fileSize, " +
                "DownloadsLeft=$numOfDownloadsLeft, ExpiryPeriod=$expiryDatetime, AllowAnonymousDownload=$allowAnonymousDownload)"
    }
}
