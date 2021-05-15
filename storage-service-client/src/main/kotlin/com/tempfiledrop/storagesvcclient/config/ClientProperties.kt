package com.tempfiledrop.storagesvcclient.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "tempfiledrop.storagesvcclient")
data class ClientProperties(
        val storageServiceUrl: String
)