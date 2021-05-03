package com.tempfiledrop.webserver.service.filestorage

import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

@Service
class FileStorageServiceImpl: FileStorageService {
    companion object {
        private val logger = LoggerFactory.getLogger(FileStorageServiceImpl::class.java)
    }

    private val root: Path = Paths.get("uploads")
    private val anonymousPath: Path = Paths.get("uploads/anonymous")

    override fun initLocalStorage() {
        try {
            logger.info("INITIALIZING FOLDER.....")
            Files.createDirectory(root)
            Files.createDirectory(anonymousPath)
        } catch (e: IOException) {
            throw RuntimeException("Could not initialize folder for upload!")
        }
    }

    override fun saveToFolder(files: List<MultipartFile>, userFolder: String) {
        try {
            // check if directory exists
            val directory = root.resolve(userFolder)
            if (!Files.exists(directory)) {
                Files.createDirectory(directory) // make directory if not exists
            }

            // check if file exists, if exists, append
            files.forEach { file ->
                Files.copy(file.inputStream, root.resolve(userFolder).resolve(file.originalFilename!!))
            }
        } catch (e: Exception) {
            throw RuntimeException("Could not store the file. Error: " + e.message)
        }
    }

    override fun loadFromFolder(filename: String): Resource {
        try {
            val file: Path = root.resolve(filename)
            val resource: Resource = UrlResource(file.toUri())
            if (resource.exists() || resource.isReadable) {
                return resource
            } else {
                throw RuntimeException("Could not read the file!")
            }
        } catch (e: MalformedURLException) {
            throw RuntimeException("Error: " + e.message)
        }
    }

    override fun deleteAllFilesInFolder() {
        logger.info("DELETING ALL FILES IN FOLDER.....")
        FileSystemUtils.deleteRecursively(root.toFile())
    }

    override fun loadAllFilesFromFolder(): Stream<Path> {
        try {
            return Files.walk(root, 1).filter { path -> path != root }.map(root::relativize)
        } catch (e: IOException) {
            throw RuntimeException("Could not load the files!")
        }
    }

}