package com.tempfiledrop.storagesvc.service.storage

import com.tempfiledrop.storagesvc.config.StorageSvcProperties
import com.tempfiledrop.storagesvc.controller.storage.StorageUploadMetadata
import com.tempfiledrop.storagesvc.exception.ApiException
import com.tempfiledrop.storagesvc.exception.ErrorCode
import com.tempfiledrop.storagesvc.service.storagefiles.StorageFile
import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfo
import com.tempfiledrop.storagesvc.util.StorageUtils
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
import java.util.*
import java.util.stream.Stream
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
        deleteAllFilesInFolder()
        try {
            logger.info("INITIALIZING FOLDER.....")
            Files.createDirectory(root)
        } catch (e: IOException) {
            throw RuntimeException("Could not initialize folder for upload!")
        }
    }

    @ExperimentalPathApi
    override fun uploadFiles(files: List<MultipartFile>, storageInfo: StorageInfo): List<StorageFile> {
        logger.info("Uploading files to Folder Storage.....")

        // create bucket if not available
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
                val fileExtension = StorageUtils.getFileExtension(it.originalFilename!!)
                val uuidFilename = "${UUID.randomUUID()}${fileExtension}"
                val filepath = bucketStoragePath.resolve(uuidFilename)
                Files.copy(it.inputStream, filepath)
                storageFiles.add(StorageFile(storageInfo.bucket, storageInfo.storagePath, it.originalFilename!!, uuidFilename, it.contentType, it.size))
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
                    val fileStoragePath = Paths.get(metadata.storagePath)
                    val bucketStoragePath = bucketPath.resolve(fileStoragePath)
                    if (!Files.exists(bucketStoragePath)) {
                        bucketStoragePath.createDirectories()
                    }

                    // copy file into bucket
                    val fileExtension = StorageUtils.getFileExtension(item.name)
                    val uuidFilename = "${UUID.randomUUID()}${fileExtension}"
                    val filepath = bucketStoragePath.resolve(uuidFilename)
                    Files.copy(item.openStream(), filepath)
                    storageFiles.add(StorageFile(metadata.bucket, metadata.storagePath, item.name, uuidFilename, item.contentType, -1))
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
        val storageInfo = StorageInfo(metadata.bucket, metadata.storagePath, filenames, metadata.maxDownloads, expiryDatetime)
        return Triple(metadata, storageInfo, storageFiles)
    }

    override fun downloadFile(storageFile: StorageFile, response: HttpServletResponse) {
        logger.info("Downloading ${storageFile.filename} from Folder Storage.....")
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

    override fun deleteFiles(storageFileList: List<StorageFile>) {
        logger.info("Deleting ${storageFileList.size} files...")
        storageFileList.forEach {
            val filepath = root.resolve(it.getFullStoragePath())
            FileSystemUtils.deleteRecursively(filepath)
        }
    }

    fun deleteAllFilesInFolder() {
        logger.info("DELETING ALL FILES IN FOLDER STORAGE.....")
        FileSystemUtils.deleteRecursively(root.toFile())
    }

    fun loadAllFilesFromFolder(): Stream<Path> {
        try {
            return Files.walk(root, 1).filter { path -> path != root }.map(root::relativize)
        } catch (e: IOException) {
            throw RuntimeException("Could not load the files!")
        }
    }
}