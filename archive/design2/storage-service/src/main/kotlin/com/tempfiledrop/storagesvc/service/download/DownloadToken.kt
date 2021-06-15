package com.tempfiledrop.storagesvc.service.download

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime

@Document(collection="download_tokens")
data class DownloadToken (
    val expiryDatetime: ZonedDateTime,
    val downloadKey: String,
    val storageId: String,
    @Id val id: String? = null
)
