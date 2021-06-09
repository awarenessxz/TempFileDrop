package com.tempfiledrop.storagesvc.service.storage

import com.tempfiledrop.storagesvc.controller.StorageUploadMetadata
import com.tempfiledrop.storagesvc.exception.ApiException
import com.tempfiledrop.storagesvc.exception.ErrorCode
import com.tempfiledrop.storagesvc.service.storagefiles.StorageFile
import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfo
import com.tempfiledrop.storagesvc.util.StorageUtils
import io.minio.*
import io.minio.messages.DeleteObject
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
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.collections.ArrayList

@Service
@ConditionalOnProperty(prefix = "storagesvc", name = ["storage-mode"], havingValue = "object")
class ObjectStorageServiceImpl(
        private val minioClient: MinioClient,
): StorageService() {
    companion object {
        private val logger = LoggerFactory.getLogger(ObjectStorageServiceImpl::class.java)
    }

    override fun initStorage() { }

    override fun uploadFiles(files: List<MultipartFile>, storageInfo: StorageInfo): List<StorageFile> {
        logger.info("Uploading files to MinIO Cluster.....")

        val storageFiles = ArrayList<StorageFile>()
        try {
            // create bucket if not available
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(storageInfo.bucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(storageInfo.bucket).build())
            }

            // upload to bucket
            val targetFolderPath = Paths.get(storageInfo.storagePath)
            files.forEach {
                logger.info("FILE ==> ${it.originalFilename}")
                val fileExtension = StorageUtils.getFileExtension(it.originalFilename!!)
                val uuidFilename = "${UUID.randomUUID()}${fileExtension}"
                val targetFilePath = targetFolderPath.resolve(uuidFilename)
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(storageInfo.bucket)
                        .contentType(it.contentType)
                        .`object`(targetFilePath.toString())
                        .stream(it.inputStream, it.size, -1)
                        .build()
                )
                storageFiles.add(StorageFile(storageInfo.bucket, storageInfo.storagePath, it.originalFilename!!, uuidFilename, it.contentType, it.size))
            }
        } catch (e: Exception) {
            throw ApiException("Could not store the files... ${e.message}", ErrorCode.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR)
        }
        return storageFiles
    }

    override fun uploadFilesViaStream(request: HttpServletRequest, isAnonymous: Boolean): Triple<StorageUploadMetadata, StorageInfo, List<StorageFile>> {
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
                    val targetFolderPath = Paths.get(metadata.storagePath)
                    val fileExtension = StorageUtils.getFileExtension(item.name)
                    val uuidFilename = "${UUID.randomUUID()}${fileExtension}"
                    val targetFilePath = targetFolderPath.resolve(uuidFilename)
                    minioClient.putObject(PutObjectArgs.builder()
                            .bucket(metadata.bucket)
                            .contentType(item.contentType)
                            .`object`(targetFilePath.toString())
                            .stream(item.openStream(), -1, 10485760)
                            .build()
                    )
                    storageFiles.add(StorageFile(metadata.bucket, metadata.storagePath, item.name!!, uuidFilename, item.contentType, -1))
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
            // zipEntry.size = it.fileLength
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