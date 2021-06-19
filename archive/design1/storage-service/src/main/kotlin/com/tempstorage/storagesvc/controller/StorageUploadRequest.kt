package com.tempstorage.storagesvc.controller

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "Storage Upload Request", description = "Additional data required when uploading files to storage service")
data class StorageUploadRequest(
        val bucket: String,
        val storagePath: String,
        val maxDownloads: Int,
        val expiryPeriod: Int,       // 0 = 1 hour, 1 = 1 day, 2 = 1 week, 3 = 1 month, 4 = No Expiry
        val eventRoutingKey: String = "#",
        val eventData: String = ""
)