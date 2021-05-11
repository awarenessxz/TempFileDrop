package com.tempfiledrop.storagesvc.controller

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "Storage Upload Response")
data class StorageResponse(
        val message: String,
        val storageId: String? = "",
        val downloadLink: String? = ""
)