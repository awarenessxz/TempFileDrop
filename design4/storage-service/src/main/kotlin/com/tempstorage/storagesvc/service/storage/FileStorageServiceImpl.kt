package com.tempstorage.storagesvc.service.storage

import com.tempstorage.storagesvc.config.StorageSvcProperties
import com.tempstorage.storagesvc.controller.storage.StorageUploadMetadata
import com.tempstorage.storagesvc.exception.ApiException
import com.tempstorage.storagesvc.exception.ErrorCode
import com.tempstorage.storagesvc.service.notification.NotificationService
import com.tempstorage.storagesvc.service.metadata.StorageMetadata
import com.tempstorage.storagesvc.util.StorageUtils
import io.minio.http.Method
import org.apache.commons.fileupload.FileItemIterator
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.util.StreamUtils
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories

@Service
@Primary
@ConditionalOnProperty(prefix = "storagesvc", name = ["storage-mode"], havingValue = "file")
class FileStorageServiceImpl(
        properties: StorageSvcProperties,
        private val notificationService: NotificationService
): StorageService() {
    companion object {
        private val logger = LoggerFactory.getLogger(FileStorageServiceImpl::class.java)
    }

    private val root: Path = Paths.get(properties.fileStorage.uploadDirectory)

    override fun initStorage() {
        // deleteAllFilesInFolder()
        try {
            logger.info("INITIALIZING FOLDER.....")
            if (!Files.exists(root)) {
                Files.createDirectory(root)
            }
        } catch (e: IOException) {
            throw RuntimeException("Could not initialize folder for upload!")
        }
    }

    override fun getS3PresignedUrl(metadata: StorageMetadata, method: Method): String? {
        return null
    }

    override fun getS3PostUploadUrl(metadata: StorageMetadata): Map<String, String>? {
        return null
    }

    @ExperimentalPathApi
    override fun uploadFilesViaStream(request: HttpServletRequest, isAnonymous: Boolean) {
        logger.info("[FILE SYSTEM] Uploading files to Folder Storage using input stream.....")

        // process upload
        val fileuploadHandler = ServletFileUpload()
        val iterStream: FileItemIterator = fileuploadHandler.getItemIterator(request)
        var metadata: StorageUploadMetadata? = null
        var bucketPath: Path? = null
        try {
            while(iterStream.hasNext()) {
                val item = iterStream.next()
                logger.info("|--- Uploaded File = ${item.name}")

                // get metadata in first loop
                if (metadata === null) {
                    // First multipart file should be metadata.
                    metadata = StorageUtils.getStorageUploadMetadata(isAnonymous, item) // shouldn't be anonymous anymore.
                    // create bucket if not available
                    bucketPath = root.resolve(metadata.bucket)
                    if (!Files.exists(bucketPath!!)) {
                        Files.createDirectory(bucketPath)
                    }
                    // continue loop if is anonymous
                    if (!isAnonymous) {
                        continue
                    }
                }

                // subsequent multipart files are uploads
                if (item.fieldName == "files" && bucketPath != null) {
                    // if path is not found, create it
                    val bucketStoragePath = bucketPath.resolve(metadata.storagePrefix!!)
                    if (!Files.exists(bucketStoragePath)) {
                        bucketStoragePath.createDirectories()
                    }

                    // copy file into bucket
                    val filepath = bucketStoragePath.resolve(item.name)
                    Files.copy(item.openStream(), filepath)
                    // define file info
                    val fileMetadata = StorageMetadata(
                            metadata.bucket,
                            listOf(metadata.storagePrefix!!, item.name).filter { it.isNotEmpty() }.joinToString("/"),
                            item.contentType,
                            -1,
                            StorageUtils.processMaxDownloadCount(metadata.maxDownloads),
                            StorageUtils.processExpiryPeriod(metadata.expiryPeriod),
                            isAnonymous || metadata.allowAnonymousDownload!!
                    )
                    // notify
                    notificationService.triggerUploadNotification(fileMetadata)
                } else {
                    throw ApiException("Invalid upload", ErrorCode.CLIENT_ERROR, HttpStatus.BAD_REQUEST)
                }
            }
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            throw ApiException("Could not store the files... ${e.message}", ErrorCode.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    override fun downloadFile(storageMetadata: StorageMetadata, response: HttpServletResponse) {
        logger.info("[FILE SYSTEM] Downloading ${storageMetadata.getOriginalFilename()} from ${storageMetadata.bucket}...")
        val filepath = root.resolve(storageMetadata.getStorageFullPath()).toString()
        val inputStream = FileInputStream(filepath)
        IOUtils.copyLarge(inputStream, response.outputStream)
        notificationService.triggerDownloadNotification(storageMetadata)
    }

    override fun downloadFilesAsZip(storageMetadataList: List<StorageMetadata>, response: HttpServletResponse) {
        logger.info("[FILE SYSTEM] Downloading ${storageMetadataList.map { it.getOriginalFilename() }} as zip...")
        val zipOut = ZipOutputStream(response.outputStream)
        storageMetadataList.forEach {
            val filepath = root.resolve(it.getStorageFullPath())
            val resource = FileSystemResource(filepath)
            val zipEntry = ZipEntry(it.getOriginalFilename())
            // zipEntry.size = resource.contentLength()
            zipOut.putNextEntry(zipEntry)
            StreamUtils.copy(resource.inputStream, zipOut)
            zipOut.closeEntry()
        }
        zipOut.finish()
        zipOut.close()
        storageMetadataList.forEach { notificationService.triggerDownloadNotification(it) }
    }

    override fun getAllFileSizeInBucket(bucket: String, storageMetadataList: List<StorageMetadata>): List<StorageMetadata> {
        logger.info("List all files and folders in Bucket - $bucket...")
        try {
            val results = Files.walk(root).filter(Files::isRegularFile).collect(Collectors.toList())
            val objectSizeMapper = results.map { it.fileName.toString() to Files.size(it) }.toMap()
            return storageMetadataList.map {
                it.fileSize = objectSizeMapper[it.getOriginalFilename()] ?: 0
                it
            }
        } catch (e: IOException) {
            throw RuntimeException("Could not load the files!")
        }
    }

    override fun deleteFile(storageMetadata: StorageMetadata) {
        logger.info("[FILE SYSTEM] Deleting ${storageMetadata.objectName} from ${storageMetadata.bucket}...")
        FileSystemUtils.deleteRecursively(root.resolve(storageMetadata.getStorageFullPath()))
        notificationService.triggerDeleteNotification(storageMetadata)
    }

//    fun deleteAllFilesInFolder() {
//        logger.info("[FILE SYSTEM] DELETING ALL FILES IN FOLDER STORAGE.....")
//        FileSystemUtils.deleteRecursively(root.toFile())
//    }
}