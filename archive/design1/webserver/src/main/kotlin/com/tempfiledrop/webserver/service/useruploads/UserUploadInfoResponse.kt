package com.tempfiledrop.webserver.service.useruploads

import com.tempfiledrop.webserver.service.storagesvcclient.StorageInfoResponse

data class UserUploadInfoResponse(
        val id: String?,
        val user: String,
        val storageInfo: StorageInfoResponse
)