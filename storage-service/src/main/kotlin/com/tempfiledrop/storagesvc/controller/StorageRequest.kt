package com.tempfiledrop.storagesvc.controller

data class StorageRequest(
        val bucket: String,
        val storagePath: String
)