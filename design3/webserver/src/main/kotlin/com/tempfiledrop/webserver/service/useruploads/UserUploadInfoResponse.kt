package com.tempfiledrop.webserver.service.useruploads

import com.tempfiledrop.webserver.model.StorageInfoResponse

data class UserUploadInfoResponse(
        val id: String?,
        val storageInfo: StorageInfoResponse
)