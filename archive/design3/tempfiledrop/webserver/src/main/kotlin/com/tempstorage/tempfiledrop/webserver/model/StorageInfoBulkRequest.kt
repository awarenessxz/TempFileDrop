package com.tempstorage.tempfiledrop.webserver.model

data class StorageInfoBulkRequest(
        val bucket: String,
        val storageIdList: List<String>
)
