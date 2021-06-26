package com.tempstorage.tempfiledrop.webserver.service.useruploads

import com.tempstorage.tempfiledrop.webserver.model.StorageInfoResponse

data class UserUploadInfoResponse(
        val id: String?,
        val storageInfo: StorageInfoResponse
)