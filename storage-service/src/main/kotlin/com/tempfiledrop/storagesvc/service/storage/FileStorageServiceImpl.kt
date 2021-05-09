package com.tempfiledrop.storagesvc.service.storage

import com.tempfiledrop.storagesvc.config.StorageSvcProperties
import com.tempfiledrop.storagesvc.exception.ApiException
import com.tempfiledrop.storagesvc.exception.ErrorCode
import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfo
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.util.StreamUtils
import org.springframework.web.multipart.MultipartFile
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.servlet.http.HttpServletResponse
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories

@Service
@Primary
@ConditionalOnProperty(prefix = "storagesvc", name = ["storage-mode"], havingValue = "file")
class FileStorageServiceImpl(
        properties: StorageSvcProperties
): FileStorageService {
    companion object {
        private val logger = LoggerFactory.getLogger(FileStorageServiceImpl::class.java)
    }

    private val root: Path = Paths.get(properties.fileStorage.uploadPath)

    override fun initStorage() {
        deleteAllFilesInFolder()
        try {
            logger.info("INITIALIZING FOLDER.....")
            Files.createDirectory(root)
        } catch (e: IOException) {
            throw RuntimeException("Could not initialize folder for upload!")
        }
    }

    @ExperimentalPathApi
    override fun uploadFiles(files: List<MultipartFile>, storageInfo: StorageInfo) {
        logger.info("Uploading files to ${storageInfo.getFullStoragePath()} in Folder Storage.....")
        // authorize & validate (is user authorize to write into this folder?)

        // if bucket is not found, throw exception (should never occur)
        val bucket = root.resolve(storageInfo.bucketName)
        if (!Files.exists(bucket)) {
            // throw ApiException("${storageInfo.bucketName} not found!", ErrorCode.BUCKET_NOT_FOUND, HttpStatus.NOT_FOUND)
            Files.createDirectory(bucket)
        }

        try {
            // if path is not found, create it
            val storagePath = Paths.get(storageInfo.storagePath)
            val bucketStoragePath = bucket.resolve(storagePath)
            if (!Files.exists(bucketStoragePath)) {
                bucketStoragePath.createDirectories()
            }

            // copy file into bucket
            files.forEach {
                val filepath = bucketStoragePath.resolve(it.originalFilename!!)
                Files.copy(it.inputStream, filepath)
            }
        } catch (e: Exception) {
            throw ApiException("Could not store the files... ${e.message}", ErrorCode.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    override fun downloadFile(storageInfo: StorageInfo, response: HttpServletResponse) {
        logger.info("Downloading ${storageInfo.storageFilename} from Folder Storage.....")
        val filepath = root.resolve(storageInfo.getFullStoragePath()).toString()
        val inputStream = FileInputStream(filepath)
        IOUtils.copyLarge(inputStream, response.outputStream)
    }

    override fun downloadFilesAsZip(storageInfoList: List<StorageInfo>, response: HttpServletResponse) {
        logger.info("Downloading files as zip from Folder Storage.....")
        val zipOut = ZipOutputStream(response.outputStream)
        storageInfoList.forEach {
            val filepath = root.resolve(it.getFullStoragePath())
            val resource = FileSystemResource(filepath)
            val zipEntry = ZipEntry(resource.filename)
            zipEntry.size = resource.contentLength()
            zipOut.putNextEntry(zipEntry)
            StreamUtils.copy(resource.inputStream, zipOut)
            zipOut.closeEntry()
        }
        zipOut.finish()
        zipOut.close()
    }

    override fun deleteFiles(storageInfoList: List<StorageInfo>) {
        logger.info("Deleting ${storageInfoList.size} files...")
        storageInfoList.forEach {
            val filepath = root.resolve(it.getFullStoragePath())
            FileSystemUtils.deleteRecursively(filepath)
        }
    }

    override fun deleteAllFilesInFolder() {
        logger.info("DELETING ALL FILES IN FOLDER STORAGE.....")
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