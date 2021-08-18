package com.tempstorage.storagesvc.service.storage

import com.tempstorage.storagesvc.controller.storage.StorageUploadMetadata
import com.tempstorage.storagesvc.exception.ApiException
import com.tempstorage.storagesvc.exception.ErrorCode
import com.tempstorage.storagesvc.service.metadata.StorageMetadata
import com.tempstorage.storagesvc.util.StorageUtils
import io.minio.*
import io.minio.http.Method
import org.apache.commons.fileupload.FileItemIterator
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.util.StreamUtils
import java.nio.file.Paths
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.collections.HashMap
import kotlin.collections.set

@Service
@ConditionalOnProperty(prefix = "storagesvc", name = ["storage-mode"], havingValue = "minio")
class MinioStorageServiceImpl(
        private val minioClient: MinioClient
): StorageService() {
    companion object {
        private val logger = LoggerFactory.getLogger(MinioStorageServiceImpl::class.java)
    }

    override fun initStorage() { }

    override fun getS3PresignedUrl(metadata: StorageMetadata, method: Method): String? {
        val reqParams: MutableMap<String, String> = HashMap()
        if (method == Method.PUT) {
            reqParams[StorageMetadata.EXPIRY_PERIOD] = metadata.expiryDatetime.toString()
            reqParams[StorageMetadata.MAX_DOWNLOAD_COUNT] = metadata.numOfDownloadsLeft.toString()
        }
        if (method == Method.GET) {
            reqParams["response-content-disposition"] =  "attachment; filename=\"${metadata.getOriginalFilename()}\""
        }
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(method)
                        .bucket(metadata.bucket)
                        .`object`(metadata.objectName)
                        .expiry(1, TimeUnit.HOURS)
                        .extraQueryParams(reqParams)
                        .build()
        )
    }

    override fun getS3PostUploadUrl(metadata: StorageMetadata): Map<String, String>? {
        val policy = PostPolicy(metadata.bucket, ZonedDateTime.now().plusHours(1))
        policy.addEqualsCondition("key", metadata.objectName)
        policy.addEqualsCondition(StorageMetadata.EXPIRY_PERIOD, metadata.expiryDatetime.toString())
        policy.addEqualsCondition(StorageMetadata.MAX_DOWNLOAD_COUNT, metadata.numOfDownloadsLeft.toString())
        val formData = minioClient.getPresignedPostFormData(policy)
        formData["key"] = metadata.objectName
        formData[StorageMetadata.EXPIRY_PERIOD] = metadata.expiryDatetime.toString()
        formData[StorageMetadata.MAX_DOWNLOAD_COUNT] = metadata.numOfDownloadsLeft.toString()
        return formData
    }

    override fun uploadFilesViaStream(request: HttpServletRequest, isAnonymous: Boolean) {
        logger.info("[MINIO CLUSTER] Uploading files to MinIO Cluster using input streams.....")

        // process upload
        val fileuploadHandler = ServletFileUpload()
        val iterStream: FileItemIterator = fileuploadHandler.getItemIterator(request)
        var metadata: StorageUploadMetadata? = null
        val userMetadata: MutableMap<String, String> = HashMap()
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
                    // generate minio metadata
                }

                // subsequent multipart files are uploads
                if (item.fieldName == "files") {
                    // upload to bucket
                    val objectName = Paths.get(metadata.storagePrefix!!).resolve(item.name!!)
                    // define metadata for object
                    val objectMetadata = StorageMetadata(
                            metadata.bucket,
                            listOf(metadata.storagePrefix!!, item.name).filter { it.isNotEmpty() }.joinToString("/"),
                            item.contentType,
                            -1,
                            StorageUtils.processMaxDownloadCount(metadata.maxDownloads),
                            StorageUtils.processExpiryPeriod(metadata.expiryPeriod),
                            isAnonymous || metadata.allowAnonymousDownload!!
                    )
                    userMetadata[StorageMetadata.EXPIRY_PERIOD] = objectMetadata.expiryDatetime.toString()
                    userMetadata[StorageMetadata.MAX_DOWNLOAD_COUNT] = objectMetadata.numOfDownloadsLeft.toString()
                    // store into minio
                    minioClient.putObject(PutObjectArgs.builder()
                            .bucket(metadata.bucket)
                            .contentType(item.contentType)
                            .`object`(objectName.toString())
                            .userMetadata(userMetadata)
                            .stream(item.openStream(), -1, 10485760)
                            .build()
                    )
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
        logger.info("[MINIO CLUSTER] Downloading ${storageMetadata.objectName} from ${storageMetadata.bucket}...")
        val objectName = storageMetadata.objectName
        val inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(storageMetadata.bucket).`object`(objectName).build())
        IOUtils.copyLarge(inputStream, response.outputStream)
    }

    override fun downloadFilesAsZip(storageMetadataList: List<StorageMetadata>, response: HttpServletResponse) {
        logger.info("[MINIO CLUSTER] Downloading ${storageMetadataList.map { it.objectName }} as zip...")
        val zipOut = ZipOutputStream(response.outputStream)
        storageMetadataList.forEach {
            val inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(it.bucket).`object`(it.objectName).build())
            val zipEntry = ZipEntry(it.getOriginalFilename())
            // zipEntry.size = it.fileLength
            zipOut.putNextEntry(zipEntry)
            StreamUtils.copy(inputStream, zipOut)
            zipOut.closeEntry()
        }
        zipOut.finish()
        zipOut.close()
    }

//    override fun getAllFileSizeInBucket(bucket: String, storageInfoList: List<StorageInfo>): List<StorageInfo> {
//        logger.info("List all files and folders in Bucket - $bucket...")
//        val results: Iterable<Result<Item>> = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucket).recursive(true).build())
//        val objectSizeMapper = results.map { it.get().objectName() to it.get().size() }.toMap()
//        return storageInfoList.map {
//            val fileSize = objectSizeMapper[it.getObjectName()] ?: 0
//            StorageInfo(it.id, it.bucket, it.storagePath, it.originalFilename, it.fileContentType, fileSize, it.numOfDownloadsLeft, it.expiryDatetime, it.allowAnonymousDownload)
//        }
//    }

    override fun deleteFile(storageMetadata: StorageMetadata) {
        logger.info("[MINIO CLUSTER] Deleting ${storageMetadata.objectName} from ${storageMetadata.bucket}...")
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(storageMetadata.bucket).`object`(storageMetadata.objectName).build())
    }
}