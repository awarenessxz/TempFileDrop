package com.tempstorage.storagesvc.service.storage

import com.tempstorage.storagesvc.config.StorageSvcProperties
import com.tempstorage.storagesvc.controller.storage.StorageUploadMetadata
import com.tempstorage.storagesvc.exception.ApiException
import com.tempstorage.storagesvc.exception.ErrorCode
import com.tempstorage.storagesvc.service.storagefiles.StorageFile
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
        properties: StorageSvcProperties
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

    @ExperimentalPathApi
    override fun uploadFiles(files: List<MultipartFile>, storageInfo: StorageInfo): List<StorageFile> {
        logger.info("Uploading files to Folder Storage.....")

        // create bucket if not available
        StorageUtils.validateBucketWithJwtToken(storageInfo.bucket)
        val bucket = root.resolve(storageInfo.bucket)
        if (!Files.exists(bucket)) {
            Files.createDirectory(bucket)
        }

        val storageFiles = ArrayList<StorageFile>()
        try {
            // if path is not found, create it
            val storagePath = Paths.get(storageInfo.storagePath)
            val bucketStoragePath = bucket.resolve(storagePath)
            if (!Files.exists(bucketStoragePath)) {
                bucketStoragePath.createDirectories()
            }

            // copy file into bucket
            files.forEach {
                logger.info("FILE ==> ${it.originalFilename}")
                val filepath = bucketStoragePath.resolve(it.originalFilename)
                Files.copy(it.inputStream, filepath)
                storageFiles.add(StorageFile(storageInfo.bucket, storageInfo.storagePath, it.originalFilename!!, it.contentType, it.size))
            }
        } catch (e: Exception) {
            throw ApiException("Could not store the files... ${e.message}", ErrorCode.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR)
        }
        return storageFiles
    }

    @ExperimentalPathApi
    override fun uploadFilesViaStream(request: HttpServletRequest, isAnonymous: Boolean): Triple<StorageUploadMetadata, StorageInfo, List<StorageFile>> {
        logger.info("Uploading files to Folder Storage using input stream.....")
        val storageFiles = ArrayList<StorageFile>()

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
                    if (!isAnonymous) {
                        StorageUtils.validateBucketWithJwtToken(metadata.bucket)
                    }
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
                    storageFiles.add(StorageFile(metadata.bucket, metadata.storagePath, item.name, item.contentType, -1))
                } else {
                    throw ApiException("Invalid upload", ErrorCode.CLIENT_ERROR, HttpStatus.BAD_REQUEST)
                }
            }
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            throw ApiException("Could not store the files... ${e.message}", ErrorCode.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR)
        }

        val filenames = storageFiles.joinToString(",") { it.originalFilename }
        val expiryDatetime = StorageUtils.processExpiryPeriod(metadata!!.expiryPeriod)
        val anonDownload = isAnonymous || metadata.allowAnonymousDownload
        val storageInfo = StorageInfo(metadata.bucket, metadata.storagePath, filenames, metadata.maxDownloads, expiryDatetime, anonDownload)
        return Triple(metadata, storageInfo, storageFiles)
    }

    override fun downloadFile(storageFile: StorageFile, response: HttpServletResponse) {
        logger.info("Downloading ${storageFile.originalFilename} from Folder Storage.....")
        val filepath = root.resolve(storageFile.getFullStoragePath()).toString()
        val inputStream = FileInputStream(filepath)
        IOUtils.copyLarge(inputStream, response.outputStream)
    }

    override fun downloadFilesAsZip(storageFiles: List<StorageFile>, response: HttpServletResponse) {
        logger.info("Downloading files as zip from Folder Storage.....")
        val zipOut = ZipOutputStream(response.outputStream)
        storageFiles.forEach {
            val filepath = root.resolve(it.getFullStoragePath())
            val resource = FileSystemResource(filepath)
            val zipEntry = ZipEntry(it.originalFilename)
            // zipEntry.size = resource.contentLength()
            zipOut.putNextEntry(zipEntry)
            StreamUtils.copy(resource.inputStream, zipOut)
            zipOut.closeEntry()
        }
        zipOut.finish()
        zipOut.close()
    }

    override fun getAllFileSizeInBucket(bucket: String, storageFiles: List<StorageFile>): List<StorageFile> {
        logger.info("List all files and folders in Bucket - $bucket...")
        try {
            val results = Files.walk(root).filter(Files::isRegularFile).collect(Collectors.toList())
            val objectSizeMapper = results.map { it.fileName.toString() to Files.size(it) }.toMap()
            return storageFiles.map {
                val fileSize = objectSizeMapper[it.originalFilename] ?: 0
                StorageFile(it.bucket, it.storagePath, it.originalFilename, it.fileContentType, fileSize, it.storageId, it.id)
            }
        } catch (e: IOException) {
            throw RuntimeException("Could not load the files!")
        }
    }

    override fun deleteFiles(storageFileList: List<StorageFile>, bucket: String) {
        logger.info("Deleting ${storageFileList.size} files from $bucket...")
        storageFileList.forEach {
            val filepath = root.resolve(it.getFullStoragePath())
            FileSystemUtils.deleteRecursively(filepath)
        }
    }

    fun deleteAllFilesInFolder() {
        logger.info("DELETING ALL FILES IN FOLDER STORAGE.....")
        FileSystemUtils.deleteRecursively(root.toFile())
    }
}