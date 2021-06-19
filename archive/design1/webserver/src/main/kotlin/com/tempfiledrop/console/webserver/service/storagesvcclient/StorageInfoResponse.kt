package com.tempfiledrop.console.webserver.service.storagesvcclient

import java.time.ZonedDateTime

data class StorageInfoResponse (
        val storageId: String,
        val downloadLink: String,
        val filenames: String,
        val numOfDownloadsLeft: Int,
        val expiryDatetime: ZonedDateTime
)