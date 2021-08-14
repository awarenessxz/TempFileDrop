package com.tempstorage.storagesvc.controller.storage

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "Storage Upload Response")
data class StorageUploadResponse(
        val message: String,
        val storageObjectList: List<String>
)