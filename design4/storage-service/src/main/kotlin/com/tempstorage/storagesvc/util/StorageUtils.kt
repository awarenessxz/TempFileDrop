package com.tempstorage.storagesvc.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.tempstorage.storagesvc.controller.storage.StorageUploadMetadata
import com.tempstorage.storagesvc.exception.ApiException
import com.tempstorage.storagesvc.exception.ErrorCode
import com.tempstorage.storagesvc.security.JwtUser
import com.tempstorage.storagesvc.service.storage.FileSystemNode
import org.apache.commons.fileupload.FileItemStream
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import java.nio.file.Files
import java.nio.file.Paths
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

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

    fun getStorageUploadMetadata(isAnonymous: Boolean, fileItemStream: FileItemStream? = null): StorageUploadMetadata {
        if (isAnonymous) {
            return StorageUploadMetadata(ANONYMOUS_BUCKET, "", 1, 1, true,"", "")
        } else {
            if(fileItemStream != null && fileItemStream.fieldName == "metadata") {
                val mapper = ObjectMapper().registerKotlinModule()
                val metadata = mapper.readValue(fileItemStream.openStream(), StorageUploadMetadata::class.java)
                val storagePath = processStoragePath(metadata.storagePath) ?: throw ApiException("Storage path is invalid", ErrorCode.UPLOAD_FAILED, HttpStatus.BAD_REQUEST)
                return StorageUploadMetadata(metadata.bucket, storagePath, metadata.maxDownloads, metadata.expiryPeriod, metadata.allowAnonymousDownload, metadata.eventRoutingKey, metadata.eventData)
            }
            throw ApiException("Metadata not found!", ErrorCode.CLIENT_ERROR, HttpStatus.BAD_REQUEST)
        }
    }

    fun buildFolderTreeStructure(bucket: String, fileSystemNodes: List<FileSystemNode>): FileSystemNode {
        val root = FileSystemNode(false, bucket, "/$bucket", bucket)
        for (fileSystemNode in fileSystemNodes) {
            buildTree(fileSystemNode.storageFullPath, fileSystemNode, root)
        }
        return root
    }

    private fun buildTree(path: String, file: FileSystemNode, parent: FileSystemNode) {
        if (path.contains("/")) {
            val currentPath = path.substring(0, path.indexOf("/"))
            val newPath = path.substring(currentPath.length + 1)
            if (parent.containsPath(currentPath)) {
                buildTree(newPath, file, parent.getFileSystemNode(currentPath)!!)
            } else {
                val newFolder = FileSystemNode(false, currentPath, "${parent.storageFullPath}/$currentPath", parent.storageBucket)
                parent.children.add(newFolder)
                buildTree(newPath, file, newFolder)
            }
        } else {
            if (!parent.containsPath(path)) {
                val newFile = FileSystemNode(true, file.label, file.storageFullPath, file.storageBucket, file.storageId, file.storageSize, file.storageDownloadLeft, file.storageExpiryDatetime)
                parent.children.add(newFile)
            }
        }
    }

    fun validateBucketWithJwtToken(bucket: String) {
        val jwtUser = SecurityContextHolder.getContext().authentication.principal as JwtUser
        if (bucket !in jwtUser.storageAttrs.buckets) {
            throw ApiException("Not Authorized to access $bucket!", ErrorCode.NOT_AUTHORIZED, HttpStatus.UNAUTHORIZED)
        }
    }

    fun validateRoutingKeysWithJwtToken(routingKey: String) {
        logger.info("${SecurityContextHolder.getContext()}")
        val jwtUser = SecurityContextHolder.getContext().authentication.principal as JwtUser
        if (routingKey !in jwtUser.storageAttrs.routingkeys) {
            throw ApiException("Not Authorized to publish message to exchange with $routingKey!", ErrorCode.NOT_AUTHORIZED, HttpStatus.UNAUTHORIZED)
        }
    }

    fun generateStorageId(): String {
        return UUID.randomUUID().toString()
    }
}