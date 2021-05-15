package com.tempfiledrop.webserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("tempfiledrop.webserver")
data class ServerProperties(
        val bucketName: String,
        val storageServiceUrl: String
)
