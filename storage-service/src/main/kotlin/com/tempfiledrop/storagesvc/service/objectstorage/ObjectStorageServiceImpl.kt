package com.tempfiledrop.storagesvc.service.objectstorage

import com.tempfiledrop.storagesvc.exception.ApiException
import com.tempfiledrop.storagesvc.exception.ErrorCode
import com.tempfiledrop.storagesvc.model.StorageInfo
import io.minio.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Paths

@Service
class ObjectStorageServiceImpl(
        private val minioClient: MinioClient
): ObjectStorageService {

    companion object {
        private val logger = LoggerFactory.getLogger(ObjectStorageServiceImpl::class.java)
    }

    override fun minioUpload(files: List<MultipartFile>, storageInfo: StorageInfo) {
        // authorize & validate (is user authorize to write into this folder?)

        try {
            // if bucket is not found, throw exception (should never occur)
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(storageInfo.bucketName).build())) {
                // throw ApiException("${storageInfo.bucketName} not found!", ErrorCode.BUCKET_NOT_FOUND, HttpStatus.NOT_FOUND)
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(storageInfo.bucketName).build())
            }

            // upload to bucket
            files.forEach {
                val targetFilePath = Paths.get(storageInfo.targetFolderPath).resolve(it.originalFilename)
                logger.info(targetFilePath.toString())
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
}