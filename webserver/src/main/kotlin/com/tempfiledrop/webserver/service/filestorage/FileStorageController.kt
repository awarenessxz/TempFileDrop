package com.tempfiledrop.webserver.service.filestorage

import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import java.util.stream.Collectors

@RestController
@RequestMapping("/files")
class FileStorageController(
        private val service: FileStorageServiceImpl
) {
    companion object {
        private val logger = LoggerFactory.getLogger(FileStorageController::class.java)
    }

    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<FileStorageResponse> {
        var message = ""
        try {
            service.saveToFolder(file)
            message = "Uploaded the file successfully: " + file.originalFilename
            val response = FileStorageResponse(message)
            return ResponseEntity(response, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error(e.message)
            message = "Could not upload the file " + file.originalFilename + "!"
            val response = FileStorageResponse(message)
            return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/list")
    fun getAllFiles(): ResponseEntity<List<FileInfo>> {
        val fileInfos: List<FileInfo> = service.loadAllFilesFromFolder().map { path ->
            val filename: String = path.fileName.toString()
            val url = MvcUriComponentsBuilder.fromMethodName(FileStorageController::class.java, "getFile", path.fileName.toString()).build().toString()
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
}