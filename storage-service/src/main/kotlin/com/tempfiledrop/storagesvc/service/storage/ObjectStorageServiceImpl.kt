package com.tempfiledrop.storagesvc.service.storage

import com.tempfiledrop.storagesvc.exception.ApiException
import com.tempfiledrop.storagesvc.exception.ErrorCode
import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfo
import io.minio.*
import io.minio.messages.DeleteObject
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
import javax.servlet.http.HttpServletResponse

@Service
@ConditionalOnProperty(prefix = "storagesvc", name = ["storage-mode"], havingValue = "object")
class ObjectStorageServiceImpl(
        private val minioClient: MinioClient
): StorageService {
    companion object {
        private val logger = LoggerFactory.getLogger(ObjectStorageServiceImpl::class.java)
    }

    override fun initStorage() { }

    override fun uploadFiles(files: List<MultipartFile>, storageInfo: StorageInfo) {
        logger.info("Uploading files to ${storageInfo.getFullStoragePath()} in MinIO Cluster.....")
        // authorize & validate (is user authorize to write into this folder?)

        try {
            // if bucket is not found, throw exception (should never occur)
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(storageInfo.bucketName).build())) {
                // throw ApiException("${storageInfo.bucketName} not found!", ErrorCode.BUCKET_NOT_FOUND, HttpStatus.NOT_FOUND)
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(storageInfo.bucketName).build())
            }

            // upload to bucket
            val targetFolderPath = Paths.get(storageInfo.storagePath)
            files.forEach {
                val targetFilePath = targetFolderPath.resolve(it.originalFilename!!)
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(storageInfo.bucketName)
                        .contentType(it.contentType)
                        .`object`(targetFilePath.toString())
                        .stream(it.inputStream, it.size, -1)
                        .build()
                )
            }
        } catch (e: Exception) {
            throw ApiException("Could not store the files... ${e.message}", ErrorCode.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    override fun downloadFile(storageInfo: StorageInfo, response: HttpServletResponse) {
        logger.info("Downloading ${storageInfo.storageFilename} from MinIO Cluster.....")
        val filepath = storageInfo.getFileStoragePath()
        val inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(storageInfo.bucketName).`object`(filepath).build())
        IOUtils.copyLarge(inputStream, response.outputStream)
    }

    override fun downloadFilesAsZip(storageInfoList: List<StorageInfo>, response: HttpServletResponse) {
        logger.info("Downloading files as zip from MinIO Cluster.....")
        val zipOut = ZipOutputStream(response.outputStream)
        storageInfoList.forEach {
            val filepath = it.getFileStoragePath()
            val inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(it.bucketName).`object`(filepath).build())
            val zipEntry = ZipEntry(it.storageFilename)
            zipEntry.size = it.storageFileLength
            zipOut.putNextEntry(zipEntry)
            StreamUtils.copy(inputStream, zipOut)
            zipOut.closeEntry()
        }
        zipOut.finish()
        zipOut.close()
    }

    override fun deleteFiles(storageInfoList: List<StorageInfo>) {
        logger.info("DELETING ALL FILES IN MINIO Cluster.....")
        val bucket = storageInfoList[0].bucketName
        val objects = storageInfoList.map {
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