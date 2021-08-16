package com.tempstorage.storagesvc.controller.storage

import com.tempstorage.storagesvc.service.metadata.StorageMetadata

data class StorageMetadataResponse(
        val storageMetadataList: Map<String, StorageMetadata>,
        val expiredObjects: List<String>
)