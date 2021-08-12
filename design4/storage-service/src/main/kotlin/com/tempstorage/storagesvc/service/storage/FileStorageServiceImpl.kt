package com.tempstorage.storagesvc.service.storage

import com.tempstorage.storagesvc.config.StorageSvcProperties
import com.tempstorage.storagesvc.controller.storage.StorageUploadMetadata
import com.tempstorage.storagesvc.controller.storage.StorageUploadResponse
import com.tempstorage.storagesvc.exception.ApiException
import com.tempstorage.storagesvc.exception.ErrorCode
import com.tempstorage.storagesvc.service.notification.NotificationService
import com.tempstorage.storagesvc.service.storageinfo.StorageInfo
import com.tempstorage.storagesvc.util.StorageUtils
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
import org.springframework.web.multipart.MultipartFile
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
import kotlin.collections.ArrayList
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

    override fun getUploadUrl(bucket: String, objectName: String): String {
        return "/api/storagesvc/upload"
    }

    @ExperimentalPathApi
    override fun uploadFilesViaStream(request: HttpServletRequest, isAnonymous: Boolean): List<StorageInfo> {
        logger.info("[FILE SYSTEM] Uploading files to Folder Storage using input stream.....")
        val uploadedFiles = ArrayList<StorageInfo>()

        // process upload
        val fileuploadHandler = ServletFileUpload()
        val iterStream: FileItemIterator = fileuploadHandler.getItemIterator(request)
        var metadata: StorageUploadMetadata? = null
        var bucketPath: Path? = null
        try {
            while(iterStream.hasNext()) {
                val item = iterStream.next()
                logger.info("FILE ==> ${item.name}")

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
                    val bucketStoragePath = bucketPath.resolve(metadata.storagePath)
                    if (!Files.exists(bucketStoragePath)) {
                        bucketStoragePath.createDirectories()
                    }

                    // copy file into bucket
                    val filepath = bucketStoragePath.resolve(item.name)
                    Files.copy(item.openStream(), filepath)
                    // extract file info
                    val tempFile = StorageInfo(
                            StorageUtils.generateStorageId(),
                            metadata.bucket,
                            metadata.storagePath,
                            item.name,
                            item.contentType,
                            -1,
                            metadata.maxDownloads,
                            StorageUtils.processExpiryPeriod(metadata.expiryPeriod),
                            isAnonymous || metadata.allowAnonymousDownload
                    )
                    uploadedFiles.add(tempFile)
                } else {
                    throw ApiException("Invalid upload", ErrorCode.CLIENT_ERROR, HttpStatus.BAD_REQUEST)
                }
            }
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            throw ApiException("Could not store the files... ${e.message}", ErrorCode.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR)
        }
        notificationService.triggerUploadNotification(uploadedFiles, metadata?.eventData ?: "")
        return uploadedFiles
    }

    override fun downloadFile(storageInfo: StorageInfo, response: HttpServletResponse, eventData: String?) {
        logger.info("[FILE SYSTEM] Downloading ${storageInfo.originalFilename} from ${storageInfo.bucket}...")
        val filepath = root.resolve(storageInfo.storageFullPath!!).toString()
        val inputStream = FileInputStream(filepath)
        IOUtils.copyLarge(inputStream, response.outputStream)
        notificationService.triggerDownloadNotification(storageInfo, eventData ?: "")
    }

//    override fun downloadFilesAsZip(storageInfo: StorageInfo, storageFiles: List<StorageFile>, response: HttpServletResponse, eventData: String?) {
//        logger.info("Downloading files as zip from Folder Storage.....")
//        val zipOut = ZipOutputStream(response.outputStream)
//        storageFiles.forEach {
//            val filepath = root.resolve(it.getFullStoragePath())
//            val resource = FileSystemResource(filepath)
//            val zipEntry = ZipEntry(it.originalFilename)
//            // zipEntry.size = resource.contentLength()
//            zipOut.putNextEntry(zipEntry)
//            StreamUtils.copy(resource.inputStream, zipOut)
//            zipOut.closeEntry()
//        }
//        zipOut.finish()
//        zipOut.close()
//        notificationService.triggerDownloadNotification(storageInfo, storageInfo.bucket, eventData ?: "")
//    }

    override fun getAllFileSizeInBucket(bucket: String, storageInfoList: List<StorageInfo>): List<StorageInfo> {
        logger.info("List all files and folders in Bucket - $bucket...")
        try {
            val results = Files.walk(root).filter(Files::isRegularFile).collect(Collectors.toList())
            val objectSizeMapper = results.map { it.fileName.toString() to Files.size(it) }.toMap()
            return storageInfoList.map {
                val fileSize = objectSizeMapper[it.originalFilename] ?: 0
                StorageInfo(it.id, it.bucket, it.storagePath, it.originalFilename, it.fileContentType, fileSize, it.numOfDownloadsLeft, it.expiryDatetime, it.allowAnonymousDownload)
            }
        } catch (e: IOException) {
            throw RuntimeException("Could not load the files!")
        }
    }

    override fun deleteFile(storageInfo: StorageInfo, eventData: String?) {
        logger.info("[FILE SYSTEM] Deleting ${storageInfo.originalFilename} from ${storageInfo.bucket}...")
        FileSystemUtils.deleteRecursively(root.resolve(storageInfo.storageFullPath!!))
        notificationService.triggerDeleteNotification(storageInfo, eventData ?: "")
    }

    fun deleteAllFilesInFolder() {
        logger.info("[FILE SYSTEM] DELETING ALL FILES IN FOLDER STORAGE.....")
        FileSystemUtils.deleteRecursively(root.toFile())
    }
}