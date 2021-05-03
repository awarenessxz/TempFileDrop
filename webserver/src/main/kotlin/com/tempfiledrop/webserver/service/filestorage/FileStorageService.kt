package com.tempfiledrop.webserver.service.filestorage

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path
import java.util.stream.Stream

interface FileStorageService {
    fun initLocalStorage()
    fun saveToFolder(files: List<MultipartFile>, userFolder: String = "anonymous")
    fun loadFromFolder(filename: String): Resource
    fun deleteAllFilesInFolder()
    fun loadAllFilesFromFolder(): Stream<Path>
}