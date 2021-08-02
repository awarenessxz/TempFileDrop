package com.tempstorage.storagesvc.service.storage

import com.tempstorage.storagesvc.controller.storage.StorageUploadMetadata
import com.tempstorage.storagesvc.exception.ApiException
import com.tempstorage.storagesvc.exception.ErrorCode
import com.tempstorage.storagesvc.service.notification.NotificationService
import com.tempstorage.storagesvc.service.storagefiles.StorageFile
import com.tempstorage.storagesvc.service.storageinfo.StorageInfo
import com.tempstorage.storagesvc.util.StorageUtils
import io.minio.*
import io.minio.messages.DeleteObject
import io.minio.messages.Item
import org.apache.commons.fileupload.FileItemIterator
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.util.StreamUtils
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.collections.ArrayList

@Service
@ConditionalOnProperty(prefix = "storagesvc", name = ["storage-mode"], havingValue = "minio")
class MinioStorageServiceImpl(
        private val minioClient: MinioClient,
        private val notificationService: NotificationService
): StorageService() {
    companion object {
        private val logger = LoggerFactory.getLogger(MinioStorageServiceImpl::class.java)
    }

    override fun initStorage() { }

    override fun uploadFilesViaStream(request: HttpServletRequest, storageId: String, isAnonymous: Boolean) {
        logger.info("Uploading files to MinIO Cluster using input streams.....")
        val storageFiles = ArrayList<StorageFile>()

        // process upload
        val fileuploadHandler = ServletFileUpload()
        val iterStream: FileItemIterator = fileuploadHandler.getItemIterator(request)
        var metadata: StorageUploadMetadata? = null
        try {
            while(iterStream.hasNext()) {
                val item = iterStream.next()
                logger.info("FILE ==> ${item.name}")

                // get metadata in first loop
                if (metadata === null) {
                    // First multipart file should be metadata.
                    metadata = StorageUtils.getStorageUploadMetadata(isAnonymous, item) // shouldn't be anonymous anymore.
                    // validate bucket
                    if (!isAnonymous) {
                        StorageUtils.validateBucketWithJwtToken(metadata.bucket)
                    }
                    // create bucket if not available
                    if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(metadata.bucket).build())) {
                        minioClient.makeBucket(MakeBucketArgs.builder().bucket(metadata.bucket).build())
                    }
                    // continue loop if is anonymous
                    if (!isAnonymous) {
                        continue
                    }
                }

                // subsequent multipart files are uploads
                if (item.fieldName == "files") {
                    // upload to bucket
                    val targetFilePath = Paths.get(metadata.storagePath).resolve(item.name!!)
                    minioClient.putObject(PutObjectArgs.builder()
                            .bucket(metadata.bucket)
                            .contentType(item.contentType)
                            .`object`(targetFilePath.toString())
                            .stream(item.openStream(), -1, 10485760)
                            .build()
                    )
                    storageFiles.add(StorageFile(metadata.bucket, metadata.storagePath, item.name!!, item.contentType, -1))
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
        val storageInfo = StorageInfo(storageId, metadata.bucket, metadata.storagePath, filenames, metadata.maxDownloads, expiryDatetime, anonDownload)
    }

    override fun downloadFile(storageFile: StorageFile, response: HttpServletResponse) {
        logger.info("Downloading ${storageFile.originalFilename} from MinIO Cluster.....")
        val filepath = storageFile.getFileStoragePath()
        val inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(storageFile.bucket).`object`(filepath).build())
        IOUtils.copyLarge(inputStream, response.outputStream)
    }

    override fun downloadFilesAsZip(storageFiles: List<StorageFile>, response: HttpServletResponse) {
        logger.info("Downloading files as zip from MinIO Cluster.....")
        val zipOut = ZipOutputStream(response.outputStream)
        storageFiles.forEach {
            val filepath = it.getFileStoragePath()
            val inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(it.bucket).`object`(filepath).build())
            val zipEntry = ZipEntry(it.originalFilename)
            // zipEntry.size = it.fileLength
            zipOut.putNextEntry(zipEntry)
            StreamUtils.copy(inputStream, zipOut)
            zipOut.closeEntry()
        }
        zipOut.finish()
        zipOut.close()
    }

    override fun getAllFileSizeInBucket(bucket: String, storageFiles: List<StorageFile>): List<StorageFile> {
        logger.info("List all files and folders in Bucket - $bucket...")
        val results: Iterable<Result<Item>> = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucket).recursive(true).build())
        val objectSizeMapper = results.map { it.get().objectName() to it.get().size() }.toMap()
        return storageFiles.map {
            val fileSize = objectSizeMapper[it.getFileStoragePath()] ?: 0
            StorageFile(it.bucket, it.storagePath, it.originalFilename, it.fileContentType, fileSize, it.storageId, it.id)
        }
    }

    override fun deleteFiles(storageFileList: List<StorageFile>, bucket: String) {
        logger.info("DELETING ALL FILES IN MINIO Cluster.....")
        if (storageFileList.isNotEmpty()) {
            val objects = storageFileList.map {
                val targetFilePath = it.getFileStoragePath()
                DeleteObject(targetFilePath)
            }
            val results = minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucket).objects(objects).build())
            for (result in results) {
                val error = result.get()
                logger.error("Error in deleting object ${error.objectName()}; ${error.message()}")
            }
        }
    }
}