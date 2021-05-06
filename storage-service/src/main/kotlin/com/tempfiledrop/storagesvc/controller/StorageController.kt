package com.tempfiledrop.storagesvc.controller

import com.tempfiledrop.storagesvc.config.StorageSvcProperties
import com.tempfiledrop.storagesvc.exception.ApiException
import com.tempfiledrop.storagesvc.exception.ErrorCode
import com.tempfiledrop.storagesvc.service.filestorage.FileInfo
import com.tempfiledrop.storagesvc.service.filestorage.FileStorageServiceImpl
import com.tempfiledrop.storagesvc.service.objectstorage.ObjectStorageServiceImpl
import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfoServiceImpl
import com.tempfiledrop.storagesvc.util.StoragePathUtils
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import java.util.stream.Collectors
import kotlin.io.path.ExperimentalPathApi

@RestController
@RequestMapping("/buckets")
class StorageController(
        private val properties: StorageSvcProperties,
        private val fileStorageService: FileStorageServiceImpl,
        private val objectStorageService: ObjectStorageServiceImpl,
        private val storageInfoService: StorageInfoServiceImpl
) {
    companion object {
        private val logger = LoggerFactory.getLogger(StorageController::class.java)
    }

    @ExperimentalPathApi
    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
            @RequestParam("files") files: List<MultipartFile>,
            @RequestPart("metadata", required = true) metadata: StorageRequest
    ): ResponseEntity<StorageResponse> {
        // process input
        logger.info("Receiving Request to store ${files.size} files in ${metadata.bucket}/${metadata.storagePath}")
        val filenamesList = files.map { it.originalFilename.toString() }
        val (isValidStoragePath, storageInfo) = StoragePathUtils.processStoragePath(metadata.bucket, metadata.storagePath)

        // validate path
        if (!isValidStoragePath) {
            val response = StorageResponse("Storage Path is invalid!")
            return ResponseEntity(response, HttpStatus.BAD_REQUEST)
        }

        // store files
        return when (properties.storageMode) {
            "file" -> {
                logger.info("[File Storage] - storing into ${storageInfo?.getFullStoragePath()}")
                fileStorageService.saveToFolder(files, storageInfo!!)
                val storageId = storageInfoService.addStorageInfo(filenamesList, storageInfo)
                val response = StorageResponse("Files uploaded successfully", storageId)
                ResponseEntity(response, HttpStatus.OK)
            }
            "object" -> {
                logger.info("[Object Storage] - storing into ${storageInfo?.getFullStoragePath()}")
                objectStorageService.minioUpload(files, storageInfo!!)
                val storageId = storageInfoService.addStorageInfo(filenamesList, storageInfo)
                val response = StorageResponse("Files uploaded successfully", storageId)
                ResponseEntity(response, HttpStatus.OK)
            }
            else ->  throw RuntimeException("Server Error: Invalid Storage Mode!!")
        }
    }

    @DeleteMapping("/{bucket}/{storageId}")
    fun deleteFilesInBucket(@PathVariable("bucket") bucket: String, @PathVariable("storageId") storageId: String): ResponseEntity<StorageResponse> {
        logger.info("Deleting Storage ID = $storageId in Bucket $bucket")
        val storageInfoList = storageInfoService.getStorageInfosByStorageId(storageId)
        if (storageInfoList.isEmpty()) {
            throw ApiException("Files not found!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST) // no files available
        }
        if (storageInfoList[0].bucketName != bucket) {
            throw ApiException("Files not found in Bucket!", ErrorCode.FILE_NOT_FOUND, HttpStatus.BAD_REQUEST) // bucket and storageID didn't match
        }

        return when (properties.storageMode) {
            "file" -> {
                fileStorageService.deleteFilesFromFolder(storageInfoList)
                storageInfoService.deleteStorageInfoById(storageId)
                val response = StorageResponse("Files deleted successfully")
                ResponseEntity(response, HttpStatus.OK)
            }
            "object" -> {
                objectStorageService.minioDeleteFiles(storageInfoList)
                storageInfoService.deleteStorageInfoById(storageId)
                val response = StorageResponse("Files deleted successfully")
                ResponseEntity(response, HttpStatus.OK)
            }
            else ->  throw RuntimeException("Server Error: Invalid Storage Mode!!")
        }
    }

    /*
    @GetMapping("/list")
    fun getAllFiles(): ResponseEntity<List<FileInfo>> {
        val fileInfos: List<FileInfo> = service.loadAllFilesFromFolder().map { path ->
            val filename: String = path.fileName.toString()
            val url = MvcUriComponentsBuilder.fromMethodName(StorageController::class.java, "getFile", path.fileName.toString()).build().toString()
            FileInfo(filename, url)
        }.collect(Collectors.toList())
        return ResponseEntity(fileInfos, HttpStatus.OK)
    }

    @GetMapping("/list/{filename:.+}")
    @ResponseBody
    fun getFile(@PathVariable filename: String): ResponseEntity<Resource> {
        val file: Resource = service.loadFromFolder(filename)
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename.toString() + "\"").body(file)
    }
     */
}