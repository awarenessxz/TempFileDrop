package com.tempstorage.storagesvc.service.storage

import com.tempstorage.storagesvc.controller.storage.StorageUploadMetadata
import com.tempstorage.storagesvc.controller.storage.StorageUploadResponse
import com.tempstorage.storagesvc.exception.ApiException
import com.tempstorage.storagesvc.exception.ErrorCode
import com.tempstorage.storagesvc.service.notification.NotificationService
import com.tempstorage.storagesvc.service.storageinfo.StorageInfo
import com.tempstorage.storagesvc.util.StorageUtils
import io.minio.*
import io.minio.messages.DeleteObject
import org.apache.commons.fileupload.FileItemIterator
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.nio.file.Paths
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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

    override fun uploadFilesViaStream(request: HttpServletRequest, isAnonymous: Boolean): StorageUploadResponse {
        logger.info("[MINIO CLUSTER] Uploading files to MinIO Cluster using input streams.....")
        val uploadedFiles = ArrayList<StorageInfo>()

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

        // TODO: remove
        notificationService.triggerUploadNotification(uploadedFiles, metadata?.eventData ?: "")
        val storageIdList = uploadedFiles.map { it.id }
        val storagePathList = uploadedFiles.map { it.storageFullPath!! }
        return StorageUploadResponse("Files uploaded successfully", storageIdList, storagePathList)
    }

    override fun downloadFile(storageInfo: StorageInfo, response: HttpServletResponse, eventData: String?) {
        logger.info("[MINIO CLUSTER] Downloading ${storageInfo.originalFilename} from ${storageInfo.bucket}...")
        val filepath = storageInfo.getStoragePathWithoutBucketPrefix()
        val inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(storageInfo.bucket).`object`(filepath).build())
        IOUtils.copyLarge(inputStream, response.outputStream)
        // TODO: remove
        notificationService.triggerDownloadNotification(storageInfo, eventData ?: "")
    }

//    override fun downloadFilesAsZip(storageInfo: StorageInfo, storageFiles: List<StorageFile>, response: HttpServletResponse, eventData: String?) {
//        logger.info("Downloading files as zip from MinIO Cluster.....")
//        val zipOut = ZipOutputStream(response.outputStream)
//        storageFiles.forEach {
//            val filepath = it.getFileStoragePath()
//            val inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(it.bucket).`object`(filepath).build())
//            val zipEntry = ZipEntry(it.originalFilename)
//            // zipEntry.size = it.fileLength
//            zipOut.putNextEntry(zipEntry)
//            StreamUtils.copy(inputStream, zipOut)
//            zipOut.closeEntry()
//        }
//        zipOut.finish()
//        zipOut.close()
//        notificationService.triggerDownloadNotification(storageInfo, storageInfo.bucket, eventData ?: "")
//    }
//
//    override fun getAllFileSizeInBucket(bucket: String, storageFiles: List<StorageFile>): List<StorageFile> {
//        logger.info("List all files and folders in Bucket - $bucket...")
//        val results: Iterable<Result<Item>> = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucket).recursive(true).build())
//        val objectSizeMapper = results.map { it.get().objectName() to it.get().size() }.toMap()
//        return storageFiles.map {
//            val fileSize = objectSizeMapper[it.getFileStoragePath()] ?: 0
//            StorageFile(it.bucket, it.storagePath, it.originalFilename, it.fileContentType, fileSize, it.storageId, it.id)
//        }
//    }

    override fun deleteFile(storageInfo: StorageInfo, eventData: String?) {
        logger.info("[MINIO CLUSTER] Deleting ${storageInfo.originalFilename} from ${storageInfo.bucket}...")
        val objectName = storageInfo.getStoragePathWithoutBucketPrefix()
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(storageInfo.bucket).`object`(objectName).build())
        // TODO: remove
        notificationService.triggerDeleteNotification(storageInfo, eventData ?: "")
    }
}