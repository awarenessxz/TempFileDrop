package com.tempfiledrop.storagesvc.util

import com.tempfiledrop.storagesvc.exception.ApiException
import com.tempfiledrop.storagesvc.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.nio.file.Files
import java.nio.file.Paths
import java.time.ZoneOffset
import java.time.ZonedDateTime

object StorageUtils {
    fun processStoragePath(path: String): String? {
        // 1. split bucket and folder path
        val splitPath = path.split("/")

        // 2. Validate target path
        splitPath.forEach {
            if (it.trim().isEmpty()) {
                return null
            }
        }
        return splitPath.joinToString("/")
    }

    fun getFileExtension(filename: String): String {
        val pos = filename.lastIndexOf(".")
        if (pos < filename.length && pos >= 0) {
            return filename.substring(pos)
        }
        return ""
    }

    fun getMediaTypeForFile(filepath: String): String {
        try {
            val path = Paths.get(filepath)
            return Files.probeContentType(path) ?: MediaType.APPLICATION_OCTET_STREAM_VALUE
        } catch (e: Exception) {
            throw ApiException("Failed to identify Mime Type", ErrorCode.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    /* *
     * Calculate the expiry date time
     * @Param
     *      expiryPeriodIdx: 0 = 1 hour, 1 = 1 day, 2 = 1 week
     */
    fun processExpiryPeriod(expiryPeriodIdx: Int): ZonedDateTime {
        val now = ZonedDateTime.now(ZoneOffset.UTC)
        return when(expiryPeriodIdx) {
            0 -> now.plusHours(1)
            1 -> now.plusDays(1)
            2 -> now.plusWeeks(1)
            3 -> now.plusMonths(1)
            else -> throw ApiException("Invalid Expiry Period!", ErrorCode.CLIENT_ERROR, HttpStatus.BAD_REQUEST)
        }
    }
}