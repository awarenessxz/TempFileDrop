package com.tempstorage.storagesvc.controller.storage

data class StorageInfoBulkRequest(
        val bucket: String,
        val storageIdList: List<String>
)
