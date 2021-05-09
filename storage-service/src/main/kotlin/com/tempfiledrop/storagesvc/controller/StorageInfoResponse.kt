package com.tempfiledrop.storagesvc.controller

data class StorageInfoResponse (
    val storageId: String,
    val downloadLink: String,
    val files: List<String>
)