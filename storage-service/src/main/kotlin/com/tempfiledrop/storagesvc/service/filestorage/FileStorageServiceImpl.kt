package com.tempfiledrop.storagesvc.service.filestorage

import com.tempfiledrop.storagesvc.config.StorageSvcProperties
import com.tempfiledrop.storagesvc.exception.ApiException
import com.tempfiledrop.storagesvc.exception.ErrorCode
import com.tempfiledrop.storagesvc.model.StorageInfo
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories

@Service
class FileStorageServiceImpl(
        properties: StorageSvcProperties
): FileStorageService {
    companion object {
        private val logger = LoggerFactory.getLogger(FileStorageServiceImpl::class.java)
    }

    private val root: Path = Paths.get(properties.fileStorage.uploadPath)

    override fun initLocalStorage() {
        try {
            logger.info("INITIALIZING FOLDER.....")
            Files.createDirectory(root)
        } catch (e: IOException) {
            throw RuntimeException("Could not initialize folder for upload!")
        }
    }

    @ExperimentalPathApi
    override fun saveToFolder(files: List<MultipartFile>, storageInfo: StorageInfo) {
        // authorize & validate (is user authorize to write into this folder?)

        // if bucket is not found, throw exception (should never occur)
        val bucket = root.resolve(storageInfo.bucketName)
        if (!Files.exists(bucket)) {
            // throw ApiException("${storageInfo.bucketName} not found!", ErrorCode.BUCKET_NOT_FOUND, HttpStatus.NOT_FOUND)
            Files.createDirectory(bucket)
        }

        try {
            // if path is not found, create it
            val bucketStoragePath = bucket.resolve(storageInfo.targetFolderPath)
            if (!Files.exists(bucketStoragePath)) {
                bucketStoragePath.createDirectories()
            }

            // copy file into bucket
            files.forEach { Files.copy(it.inputStream, bucketStoragePath.resolve(it.originalFilename!!)) }
        } catch (e: Exception) {
            throw ApiException("Could not store the files... ${e.message}", ErrorCode.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    override fun loadFromFolder(filename: String): Resource {
        try {
            val file: Path = root.resolve(filename)
            val resource: Resource = UrlResource(file.toUri())
            if (resource.exists() || resource.isReadable) {
                return resource
            } else {
                throw RuntimeException("Could not read the file!")
            }
        } catch (e: MalformedURLException) {
            throw RuntimeException("Error: " + e.message)
        }
    }

    override fun deleteAllFilesInFolder() {
        logger.info("DELETING ALL FILES IN FOLDER.....")
        FileSystemUtils.deleteRecursively(root.toFile())
    }

    override fun loadAllFilesFromFolder(): Stream<Path> {
        try {
            return Files.walk(root, 1).filter { path -> path != root }.map(root::relativize)
        } catch (e: IOException) {
            throw RuntimeException("Could not load the files!")
        }
    }
}