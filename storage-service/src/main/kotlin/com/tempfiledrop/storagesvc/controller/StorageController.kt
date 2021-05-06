package com.tempfiledrop.storagesvc.controller

import com.tempfiledrop.storagesvc.config.StorageSvcProperties
import com.tempfiledrop.storagesvc.service.filestorage.FileInfo
import com.tempfiledrop.storagesvc.service.filestorage.FileStorageServiceImpl
import com.tempfiledrop.storagesvc.service.objectstorage.ObjectStorageServiceImpl
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
        private val objectStorageService: ObjectStorageServiceImpl
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
        val storageInfo = StoragePathUtils.processStoragePath(metadata.storagePath, files.size)

        // validate path
        if (!storageInfo.isValidStoragePath) {
            val response = StorageResponse("Storage Path is invalid!")
            return ResponseEntity(response, HttpStatus.BAD_REQUEST)
        }

        // store files
        return when (properties.storageMode) {
            "file" -> {
                fileStorageService.saveToFolder(files, storageInfo)
                val response = StorageResponse("Files uploaded successfully", "http://??????")
                ResponseEntity(response, HttpStatus.OK)
            }
            "object" -> {
                objectStorageService.minioUpload(files, storageInfo)
                val response = StorageResponse("Files uploaded successfully", "http://??????")
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