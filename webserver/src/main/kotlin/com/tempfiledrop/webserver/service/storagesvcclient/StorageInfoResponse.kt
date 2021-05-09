package com.tempfiledrop.webserver.service.storagesvcclient

data class StorageInfoResponse (
        val storageId: String,
        val downloadLink: String,
        val files: List<String>
)