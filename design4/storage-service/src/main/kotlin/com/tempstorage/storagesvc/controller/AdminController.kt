//package com.tempstorage.storagesvc.controller
//
//import com.tempstorage.storagesvc.service.eventdata.EventData
//import com.tempstorage.storagesvc.service.eventdata.EventDataService
//import com.tempstorage.storagesvc.service.storage.FileSystemNode
//import com.tempstorage.storagesvc.service.storage.StorageService
//import com.tempstorage.storagesvc.service.storageinfo.StorageInfo
//import io.swagger.v3.oas.annotations.Operation
//import org.slf4j.LoggerFactory
//import org.springframework.http.HttpStatus
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.*
//
//@RestController
//@RequestMapping("/api/storagesvc/admin")
//class AdminController(
//        private val storageService: StorageService,
//        private val eventDataService: EventDataService
//) {
//    companion object {
//        private val logger = LoggerFactory.getLogger(AdminController::class.java)
//    }
//
//    @Operation(summary = "Get all files and folders inside bucket in folder like structure")
//    @GetMapping("/list")
//    fun getStorageFromBucket(): ResponseEntity<List<FileSystemNode>> {
//        logger.info("Receiving Request to get content from all buckets")
//        val buckets = storageService.getAllBuckets()
//        val results = buckets.map {
//            storageService.listFilesAndFoldersInBucket(it)
//        }
//        return ResponseEntity(results, HttpStatus.OK)
//    }
//
//    @Operation(summary = "Get number of files in storage")
//    @GetMapping("/storage-info")
//    fun getBucketInfo(): ResponseEntity<List<StorageInfo>> {
//        logger.info("Receiving Request to get number of files from all buckets")
//        val results = storageService.getAllStorageInfo()
//        return ResponseEntity(results, HttpStatus.OK)
//    }
//
//    @Operation(summary = "Get all events that were published to message queue")
//    @GetMapping("/events")
//    fun getAllEvents(): ResponseEntity<List<EventData>> {
//        logger.info("Receiving Request to get all events that were published to message queue")
//        val events = eventDataService.getAllEventsFromDB()
//        return ResponseEntity(events, HttpStatus.OK)
//    }
//}