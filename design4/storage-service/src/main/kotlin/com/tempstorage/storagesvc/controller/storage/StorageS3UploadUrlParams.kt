package com.tempstorage.storagesvc.controller.storage

data class StorageS3UploadUrlParams(
        val bucket: String,
        val storageObjects: List<String>,                   // required for s3 uploads
        val maxDownloads: Int? = 1,
        val expiryPeriod: Int? = 1,                         // 0 = 1 hour, 1 = 1 day, 2 = 1 week, 3 = 1 month, 4 = No Expiry
        val allowAnonymousDownload: Boolean? = false,
        val customEventData: String? = ""                   // custom data to be included in notification
)