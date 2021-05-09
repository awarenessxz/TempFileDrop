package com.tempfiledrop.storagesvc.util

import com.tempfiledrop.storagesvc.exception.ApiException
import com.tempfiledrop.storagesvc.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.nio.file.Files
import java.nio.file.Paths

object StorageUtils {
    fun processStoragePath(path: String): Pair<Boolean, String?> {
        // 1. split bucket and folder path
        val splitPath = path.split("/")

        // 2. Validate target path
        splitPath.forEach {
            if (it.trim().isEmpty()) {
                return Pair(false, null)
            }
        }
        val storagePath = splitPath.joinToString("/")

        return Pair(true, storagePath)
    }

    fun getMediaTypeForFile(filepath: String): String {
        try {
            val path = Paths.get(filepath)
            return Files.probeContentType(path) ?: MediaType.APPLICATION_OCTET_STREAM_VALUE
        } catch (e: Exception) {
            throw ApiException("Failed to identify Mime Type", ErrorCode.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}