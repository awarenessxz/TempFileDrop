package com.tempstorage.storagesvc.controller

data class StorageInfoBulkRequest(
        val bucket: String,
        val storageIdList: List<String>
)
