package com.tempfiledrop.storagesvcclient.model

data class StorageUploadResponse(
        val message: String,
        val storageId: String,
        val downloadLink: String
)