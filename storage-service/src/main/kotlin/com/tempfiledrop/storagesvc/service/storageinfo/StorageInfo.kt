package com.tempfiledrop.storagesvc.service.storageinfo

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.nio.file.Paths
import java.time.ZonedDateTime

@Document(collection="storage_info")
data class StorageInfo(
        val bucketName: String,                    // bucket_name
        val storagePath: String,                   // the directory
        val storageFilename: String = "",          // file name
        val storageFileContentType: String? = "",  // content type
        val storageFileLength: Long = 0,           // file size
        val storageId: String = "",                // group ID for all the files that are uploaded at the same time
        @Id val id: ObjectId = ObjectId.get()      // mongoDB ID
) {
    fun getFullStoragePath(): String {
        return Paths.get(bucketName).resolve(storagePath).resolve(storageFilename).toString()
    }

    fun getFileStoragePath(): String {
        return Paths.get(storagePath).resolve(storageFilename).toString()
    }
}