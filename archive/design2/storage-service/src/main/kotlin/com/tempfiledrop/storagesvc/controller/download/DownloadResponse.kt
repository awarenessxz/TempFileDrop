package com.tempfiledrop.storagesvc.controller.download

import java.time.ZonedDateTime

data class DownloadResponse(
        val downloadEndpoint: String,
        val tokenExpiryDateTime: ZonedDateTime,
        val numOfDownloadsLeft: Int,
        val expiryDatetime: ZonedDateTime,
        val requiresAuthentication: Boolean
)