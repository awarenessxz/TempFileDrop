package com.tempfiledrop.storagesvc.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "tempfiledrop.storagesvc")
data class StorageSvcProperties(
        val storageMode: String = "object",
        val fileStorage: FileStorageProps,
        val objectStorage: ObjectStorageProps
)

data class FileStorageProps(val uploadDirectory: String)

data class ObjectStorageProps(
        val minioEndpoint: String,
        val minioAccessKey: String,
        val minioAccessSecret: String
)
