package com.tempstorage.storagesvc.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.tempstorage.storagesvc.controller.storage.StorageUploadMetadata
import com.tempstorage.storagesvc.exception.ApiException
import com.tempstorage.storagesvc.exception.ErrorCode
import com.tempstorage.storagesvc.service.storage.FileSystemNode
import org.apache.commons.fileupload.FileItemStream
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.nio.file.Files
import java.nio.file.Paths
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.collections.ArrayList

object StorageUtils {
    private const val ANONYMOUS_BUCKET = "anonymous"
    private val logger = LoggerFactory.getLogger(StorageUtils::class.java)

    private fun processStoragePath(path: String): String? {
        // 1. Check for empty string
        if (path.isEmpty()) {
            return path
        }

        // 2. split bucket and folder path
        val splitPath = path.split("/")

        // 3. Validate target path
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
    fun processExpiryPeriod(expiryPeriodIdx: Int?): ZonedDateTime? {
        val now = ZonedDateTime.now(ZoneOffset.UTC)
        return when(expiryPeriodIdx) {
            0 -> now.plusHours(1)
            1 -> now.plusDays(1)
            2 -> now.plusWeeks(1)
            3 -> now.plusMonths(1)
            else -> null
        }
    }

    fun processMaxDownloadCount(maxDownloadCount: Int?): Int {
        return maxDownloadCount ?: -999
    }

    fun getStorageUploadMetadata(isAnonymous: Boolean, fileItemStream: FileItemStream? = null): StorageUploadMetadata {
        if (isAnonymous) {
            return StorageUploadMetadata(bucket = ANONYMOUS_BUCKET, allowAnonymousDownload =  true)
        } else {
            if(fileItemStream != null && fileItemStream.fieldName == "metadata") {
                val mapper = ObjectMapper().registerKotlinModule()
                return mapper.readValue(fileItemStream.openStream(), StorageUploadMetadata::class.java)
            }
            throw ApiException("Metadata not found!", ErrorCode.CLIENT_ERROR, HttpStatus.BAD_REQUEST)
        }
    }

//    fun buildFolderTreeStructure(bucket: String, fileSystemNodes: List<FileSystemNode>): FileSystemNode {
//        val root = FileSystemNode(false, bucket, "/$bucket", bucket)
//        for (fileSystemNode in fileSystemNodes) {
//            buildTree(fileSystemNode.storageFullPath, fileSystemNode, root)
//        }
//        return root
//    }
//
//    private fun buildTree(path: String, file: FileSystemNode, parent: FileSystemNode) {
//        if (path.contains("/")) {
//            val currentPath = path.substring(0, path.indexOf("/"))
//            val newPath = path.substring(currentPath.length + 1)
//            if (parent.containsPath(currentPath)) {
//                buildTree(newPath, file, parent.getFileSystemNode(currentPath)!!)
//            } else {
//                val newFolder = FileSystemNode(false, currentPath, "${parent.storageFullPath}/$currentPath", parent.storageBucket)
//                parent.children.add(newFolder)
//                buildTree(newPath, file, newFolder)
//            }
//        } else {
//            if (!parent.containsPath(path)) {
//                val newFile = FileSystemNode(true, file.label, file.storageFullPath, file.storageBucket, file.storageId, file.storageSize, file.storageDownloadLeft, file.storageExpiryDatetime)
//                parent.children.add(newFile)
//            }
//        }
//    }
}