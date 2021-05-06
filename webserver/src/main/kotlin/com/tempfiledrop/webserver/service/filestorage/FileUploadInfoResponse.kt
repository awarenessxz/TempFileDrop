package com.tempfiledrop.webserver.service.filestorage

data class FileUploadInfoResponse(
        val message: String,
        val storageId: String,
        val downloadLink: String
)