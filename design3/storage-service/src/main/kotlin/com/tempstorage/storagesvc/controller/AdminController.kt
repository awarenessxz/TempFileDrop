package com.tempstorage.storagesvc.controller

import com.tempstorage.storagesvc.service.storage.FileSystemNode
import com.tempstorage.storagesvc.service.storage.StorageService
import com.tempstorage.storagesvc.service.storageinfo.StorageInfoService
import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/storagesvc/admin")
class AdminController(
        private val storageInfoService: StorageInfoService,
        private val storageService: StorageService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(AdminController::class.java)
    }

    @Operation(summary = "Get all files and folders inside bucket in folder like structure")
    @GetMapping("/list")
    fun getStorageFromBucket(): ResponseEntity<List<FileSystemNode>> {
        logger.info("Receiving Request to get content from all buckets")
        val buckets = storageInfoService.getBuckets()
        val results = buckets.map {
            storageService.listFilesAndFoldersInBucket(it)
        }
        return ResponseEntity(results, HttpStatus.OK)
    }
}