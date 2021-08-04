package com.tempstorage.storagesvc.controller.storage

import com.tempstorage.storagesvc.service.storageinfo.StorageInfo

data class StorageInfoBulkResponse(
        val storageInfoList: List<StorageInfo>
)