package com.tempstorage.tempfiledrop.webserver.model

data class StorageMetadataResponse(
        val storageMetadataList: Map<String, StorageMetadata>,
        val expiredObjects: List<String>
)