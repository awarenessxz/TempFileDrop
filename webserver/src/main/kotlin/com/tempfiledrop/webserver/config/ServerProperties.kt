package com.tempfiledrop.webserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("tempfiledrop")
data class ServerProperties(
        val bucketName: String,
        val storageServiceUrl: String,
        //val apiErrorCodes: Map<String, Int> = HashMap()
)
