package com.tempstorage.storagesvc.controller.storage

import com.tempstorage.storagesvc.service.metadata.StorageMetadata

data class StorageInfoBulkResponse(
        val storageMetadataList: List<StorageMetadata>
)