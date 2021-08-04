package com.tempstorage.tempfiledrop.webserver.service.useruploads

import com.tempstorage.tempfiledrop.webserver.model.StorageInfo

data class UserUploadInfoResponse(
        val id: String?,
        val storageInfo: StorageInfo
)