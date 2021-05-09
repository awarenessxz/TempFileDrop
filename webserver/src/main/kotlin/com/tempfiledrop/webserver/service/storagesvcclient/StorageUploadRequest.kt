package com.tempfiledrop.webserver.service.storagesvcclient

data class StorageUploadRequest(
        val bucket: String,
        val storagePath: String
)