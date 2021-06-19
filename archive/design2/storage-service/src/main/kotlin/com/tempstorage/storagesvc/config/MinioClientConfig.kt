package com.tempstorage.storagesvc.config

import io.minio.MinioClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioClientConfig(
        private val properties: StorageSvcProperties
) {
    @Bean
    fun generateMinioClient(): MinioClient {
        try {
            val minioConfig = properties.objectStorage
            return MinioClient.builder()
                    .endpoint(minioConfig.minioEndpoint)
                    .credentials(minioConfig.minioAccessKey, minioConfig.minioAccessSecret)
                    .build()
        } catch (e: Exception) {
            throw RuntimeException(e.message)
        }
    }
}