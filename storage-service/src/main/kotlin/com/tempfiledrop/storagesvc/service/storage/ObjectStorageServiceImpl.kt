package com.tempfiledrop.storagesvc.service.storage

import com.tempfiledrop.storagesvc.exception.ApiException
import com.tempfiledrop.storagesvc.exception.ErrorCode
import com.tempfiledrop.storagesvc.service.storagefiles.StorageFile
import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfo
import com.tempfiledrop.storagesvc.util.StorageUtils
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
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.servlet.http.HttpServletResponse
import kotlin.collections.ArrayList

@Service
@ConditionalOnProperty(prefix = "tempfiledrop.storagesvc", name = ["storage-mode"], havingValue = "object")
class ObjectStorageServiceImpl(
        private val minioClient: MinioClient
): StorageService {
    companion object {
        private val logger = LoggerFactory.getLogger(ObjectStorageServiceImpl::class.java)
    }

    override fun initStorage() { }

    override fun uploadFiles(files: List<MultipartFile>, storageInfo: StorageInfo): List<StorageFile> {
        logger.info("Uploading files to MinIO Cluster.....")
        // authorize & validate (is user authorize to write into this folder?)

        val storageFiles = ArrayList<StorageFile>()
        try {
            // if bucket is not found, throw exception (should never occur)
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(storageInfo.bucketName).build())) {
                // throw ApiException("${storageInfo.bucketName} not found!", ErrorCode.BUCKET_NOT_FOUND, HttpStatus.NOT_FOUND)
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(storageInfo.bucketName).build())
            }

            // upload to bucket
            val targetFolderPath = Paths.get(storageInfo.storagePath)
            files.forEach {
                val fileExtension = StorageUtils.getFileExtension(it.originalFilename!!)
                val uuidFilename = "${UUID.randomUUID()}${fileExtension}"
                val targetFilePath = targetFolderPath.resolve(uuidFilename)
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(storageInfo.bucketName)
                        .contentType(it.contentType)
                        .`object`(targetFilePath.toString())
                        .stream(it.inputStream, it.size, -1)
                        .build()
                )
                storageFiles.add(StorageFile(storageInfo.bucketName, storageInfo.storagePath, it.originalFilename!!, uuidFilename, it.contentType, it.size))
            }
        } catch (e: Exception) {
            throw ApiException("Could not store the files... ${e.message}", ErrorCode.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR)
        }
        return storageFiles
    }

    override fun downloadFile(storageFile: StorageFile, response: HttpServletResponse) {
        logger.info("Downloading ${storageFile.filename} from MinIO Cluster.....")
        val filepath = storageFile.getFileStoragePath()
        val inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(storageFile.bucketName).`object`(filepath).build())
        IOUtils.copyLarge(inputStream, response.outputStream)
    }

    override fun downloadFilesAsZip(storageFiles: List<StorageFile>, response: HttpServletResponse) {
        logger.info("Downloading files as zip from MinIO Cluster.....")
        val zipOut = ZipOutputStream(response.outputStream)
        storageFiles.forEach {
            val filepath = it.getFileStoragePath()
            val inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(it.bucketName).`object`(filepath).build())
            val zipEntry = ZipEntry(it.originalFilename)
            zipEntry.size = it.fileLength
            zipOut.putNextEntry(zipEntry)
            StreamUtils.copy(inputStream, zipOut)
            zipOut.closeEntry()
        }
        zipOut.finish()
        zipOut.close()
    }

    override fun deleteFiles(storageFileList: List<StorageFile>) {
        logger.info("DELETING ALL FILES IN MINIO Cluster.....")
        val bucket = storageFileList[0].bucketName
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