package com.tempfiledrop.webserver.service.filestorage

data class FileUploadInfoRequest (
        val username: String,
        val maxDownloads: Int,
        val expiryPeriod: Int       // 0 = 1 hour, 1 = 1 day, 2 = 1 week
)