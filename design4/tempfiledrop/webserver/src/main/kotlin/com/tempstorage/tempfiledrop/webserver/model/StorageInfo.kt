package com.tempstorage.tempfiledrop.webserver.model

import java.time.ZonedDateTime

data class StorageInfo (
        val id: String,
        val bucket: String,
        val storagePath: String,
        val originalFilename: String,
        val fileContentType: String?,
        val fileLength: Long,
        val numOfDownloadsLeft: Int,
        val expiryDatetime: ZonedDateTime,
        val allowAnonymousDownload: Boolean,
        val storageFullPath: String
)