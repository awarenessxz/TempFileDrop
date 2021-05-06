package com.tempfiledrop.storagesvc.service.storageinfo

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection="storage_info")
data class StorageInfo(
        val bucketName: String,                    // bucket_name
        val storagePath: String,                   // the directory
        val storageFile: String = "",              // the file
        val storageId: String = "",                // group ID for all the files that are uploaded at the same time
        @Id val id: ObjectId = ObjectId.get()      // mongoDB ID
) {
    fun getFullStoragePath(): String {
        return "$bucketName/$storagePath/$storageFile"
    }
}