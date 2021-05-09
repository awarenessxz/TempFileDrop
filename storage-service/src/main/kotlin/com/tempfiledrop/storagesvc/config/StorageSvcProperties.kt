package com.tempfiledrop.storagesvc.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("storagesvc")
data class StorageSvcProperties(
        val exposeEndpoint: String,
        val storageMode: String = "object",
        val fileStorage: FileStorageProps,
        val objectStorage: ObjectStorageProps
)

data class FileStorageProps(val uploadPath: String)

data class ObjectStorageProps(
        val minioEndpoint: String,
        val minioAccessKey: String,
        val minioAccessSecret: String
)
