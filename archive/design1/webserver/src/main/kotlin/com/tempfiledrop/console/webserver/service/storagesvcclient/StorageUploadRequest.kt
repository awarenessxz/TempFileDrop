package com.tempfiledrop.console.webserver.service.storagesvcclient

data class StorageUploadRequest(
        val bucket: String,
        val storagePath: String,
        val maxDownloads: Int,
        val expiryPeriod: Int       // 0 = 1 hour, 1 = 1 day, 2 = 1 week, 3 = 1 month, 4 = No Expiry
)