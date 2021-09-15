package com.tempstorage.storagesvc.service.storage

import java.time.ZonedDateTime

data class FileSystemNode(
        val isFile: Boolean,
        val label: String,
        val storageFullPath: String,
        val storageBucket: String,
        val storageSize: Int = 0,
        val storageDownloadLeft: Int = 0,
        val storageExpiryDatetime: ZonedDateTime? = null,
        val children: MutableList<FileSystemNode> = ArrayList()
) {
    fun containsPath(path: String): Boolean {
        return children.any { it.label == path }
    }

    fun getFileSystemNode(path: String): FileSystemNode? {
        return children.find { it.label == path }
    }
}