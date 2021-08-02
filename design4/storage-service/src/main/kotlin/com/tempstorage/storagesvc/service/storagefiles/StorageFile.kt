package com.tempstorage.storagesvc.service.storagefiles

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.nio.file.Paths

@Document(collection="storage_files")
data class StorageFile(
        val bucket: String,         // bucket_name
        val storagePath: String,        // the directory
        val originalFilename: String,   // original file name
        val fileContentType: String?,   // content type
        val fileLength: Long,           // file size
        val storageId: String? = null,  // group ID for all the files that are uploaded at the same time
        @Id val id: String? = null      // mongoDB ID
) {
    fun getFullStoragePath(): String {
        return Paths.get(bucket).resolve(storagePath).resolve(originalFilename).toString()
    }

    fun getFileStoragePath(): String {
        return Paths.get(storagePath).resolve(originalFilename).toString()
    }
}