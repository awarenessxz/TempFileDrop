package com.tempstorage.tempfiledrop.webserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("tempfiledrop.storagesvc-client")
data class StorageSvcClientProperties(
    val storageServiceUrl: String
)
