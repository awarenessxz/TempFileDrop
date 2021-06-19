package com.tempfiledrop.console.webserver.service.storagesvcclient

data class StorageUploadResponse(
        val message: String,
        val storageId: String,
        val downloadLink: String
)