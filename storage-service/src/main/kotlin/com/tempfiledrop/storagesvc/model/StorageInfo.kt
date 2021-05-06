package com.tempfiledrop.storagesvc.model

data class StorageInfo(
        val isValidStoragePath: Boolean,
        val bucketName: String = "",
        val targetFolderPath: String = "",
        private val numOfFiles: Int = 0
)