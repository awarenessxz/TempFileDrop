package com.tempstorage.storagesvc.controller.storage

import java.time.ZonedDateTime

data class StorageInfoResponse (
        val storageId: String,
        val filenames: String,
        val numOfDownloadsLeft: Int,
        val expiryDatetime: ZonedDateTime,
        val allowAnonymousDownload: Boolean
)