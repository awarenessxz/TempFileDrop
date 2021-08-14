package com.tempstorage.storagesvc.service.storage

import com.tempstorage.storagesvc.controller.storage.StorageUploadMetadata
import com.tempstorage.storagesvc.exception.ApiException
import com.tempstorage.storagesvc.exception.ErrorCode
import com.tempstorage.storagesvc.service.notification.NotificationService
import com.tempstorage.storagesvc.service.storageinfo.StorageInfo
import com.tempstorage.storagesvc.service.storageinfo.StorageStatus
import com.tempstorage.storagesvc.util.StorageUtils
import io.minio.*
import io.minio.http.Method
import org.apache.commons.fileupload.FileItemIterator
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.nio.file.Paths
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest


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

    override fun getS3PutUploadUrl(bucket: String, objectName: String): String? {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(bucket)
                        .`object`(objectName)
                        .expiry(1, TimeUnit.HOURS)
                        .build()
        )
    }

    override fun getS3PostUploadUrl(bucket: String, objectName: String): Map<String, String>? {
        val policy = PostPolicy(bucket, ZonedDateTime.now().plusHours(1))
        policy.addEqualsCondition("key", objectName)
        val formData = minioClient.getPresignedPostFormData(policy)
        formData["key"] = objectName
        return formData
    }

    override fun uploadFilesViaStream(request: HttpServletRequest, isAnonymous: Boolean): List<StorageInfo> {
        logger.info("[MINIO CLUSTER] Uploading files to MinIO Cluster using input streams.....")
        val uploadedFiles = ArrayList<StorageInfo>()

        // process upload
        val fileuploadHandler = ServletFileUpload()
        val iterStream: FileItemIterator = fileuploadHandler.getItemIterator(request)
        var metadata: StorageUploadMetadata? = null
        try {
            while(iterStream.hasNext()) {
                val item = iterStream.next()
                logger.info("|--- Uploaded File = ${item.name}")

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
                    val objectName = Paths.get(metadata.storagePrefix!!).resolve(item.name!!)
                    minioClient.putObject(PutObjectArgs.builder()
                            .bucket(metadata.bucket)
                            .contentType(item.contentType)
                            .`object`(objectName.toString())
                            .stream(item.openStream(), -1, 10485760)
                            .build()
                    )
                    // extract file info
                    val tempFile = StorageInfo(
                            metadata.bucket,
                            listOf(metadata.storagePrefix!!, item.name).filter { it.isNotEmpty() }.joinToString("/"),
                            item.contentType,
                            -1,
                            metadata.maxDownloads!!,
                            StorageUtils.processExpiryPeriod(metadata.expiryPeriod!!),
                            isAnonymous || metadata.allowAnonymousDownload!!,
                            StorageStatus.UPLOADED
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
        return uploadedFiles
    }

//    override fun downloadFile(storageInfo: StorageInfo, response: HttpServletResponse, eventData: String?) {
//        logger.info("[MINIO CLUSTER] Downloading ${storageInfo.originalFilename} from ${storageInfo.bucket}...")
//        val objectName = storageInfo.getObjectName()
//        val inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(storageInfo.bucket).`object`(objectName).build())
//        IOUtils.copyLarge(inputStream, response.outputStream)
//    }

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

//    override fun getAllFileSizeInBucket(bucket: String, storageInfoList: List<StorageInfo>): List<StorageInfo> {
//        logger.info("List all files and folders in Bucket - $bucket...")
//        val results: Iterable<Result<Item>> = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucket).recursive(true).build())
//        val objectSizeMapper = results.map { it.get().objectName() to it.get().size() }.toMap()
//        return storageInfoList.map {
//            val fileSize = objectSizeMapper[it.getObjectName()] ?: 0
//            StorageInfo(it.id, it.bucket, it.storagePath, it.originalFilename, it.fileContentType, fileSize, it.numOfDownloadsLeft, it.expiryDatetime, it.allowAnonymousDownload)
//        }
//    }
//
//    override fun deleteFile(storageInfo: StorageInfo, eventData: String?) {
//        logger.info("[MINIO CLUSTER] Deleting ${storageInfo.originalFilename} from ${storageInfo.bucket}...")
//        val objectName = storageInfo.getObjectName()
//        minioClient.removeObject(RemoveObjectArgs.builder().bucket(storageInfo.bucket).`object`(objectName).build())
//    }
}