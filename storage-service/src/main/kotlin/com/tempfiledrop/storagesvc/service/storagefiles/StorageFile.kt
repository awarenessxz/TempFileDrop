package com.tempfiledrop.storagesvc.service.storagefiles

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.nio.file.Paths

@Document(collection="storage_files")
data class StorageFile(
        val bucketName: String,         // bucket_name
        val storagePath: String,        // the directory
        val filename: String,           // file name
        val fileContentType: String?,   // content type
        val fileLength: Long,           // file size
        val storageId: String,          // group ID for all the files that are uploaded at the same time
        @Id val id: String? = null      // mongoDB ID
) {
    fun getFullStoragePath(): String {
        return Paths.get(bucketName).resolve(storagePath).resolve(filename).toString()
    }

    fun getFileStoragePath(): String {
        return Paths.get(storagePath).resolve(filename).toString()
    }
}