package com.tempfiledrop.storagesvc.controller

data class StorageResponse(
        val message: String,
        val storageId: String? = "",
        val downloadLink: String? = ""
)