package com.tempfiledrop.webserver.service.storagesvcclient

data class StorageRequest(
        val bucket: String,
        val storagePath: String,
        val maxDownloads: Int? = 0,
        val expiryPeriod: Int? = 0       // 0 = 1 hour, 1 = 1 day, 2 = 1 week
)