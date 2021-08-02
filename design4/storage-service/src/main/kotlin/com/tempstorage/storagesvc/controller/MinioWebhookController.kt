//package com.tempstorage.storagesvc.controller
//
//import io.swagger.v3.oas.annotations.Operation
//import org.slf4j.LoggerFactory
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
//import org.springframework.web.bind.annotation.PostMapping
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//@RequestMapping("/webhook/storagesvc")
//@ConditionalOnProperty(prefix = "storagesvc", name = ["storage-mode"], havingValue = "minio")
//class MinioWebhookController {
//    companion object {
//        private val logger = LoggerFactory.getLogger(MinioWebhookController::class.java)
//    }
//
//    @Operation(summary = "Get number of files in bucket")
//    @PostMapping("/upload")
//    fun
//
//}