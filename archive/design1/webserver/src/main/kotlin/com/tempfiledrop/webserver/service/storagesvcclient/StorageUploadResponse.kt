package com.tempfiledrop.webserver.service.storagesvcclient

data class StorageUploadResponse(
        val message: String,
        val storageId: String,
        val downloadLink: String
)