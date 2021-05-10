package com.tempfiledrop.webserver.service.swaggerproxy

import com.tempfiledrop.webserver.service.storagesvcclient.StorageSvcClientImpl
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/swagger")
class SwaggerProxyController(
        private val storageSvcClient: StorageSvcClientImpl
) {
    @GetMapping("/storagesvc")
    fun getStorageServiceSwagger(): ResponseEntity<Resource> {
        return storageSvcClient.getSwagger()
    }
}