package com.tempstorage.storagesvc.controller.storage

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "Storage S3 Presigned Url Response")
data class StorageS3PresignedUrlResponse(
        val s3PresignedUrls: Map<String, String>,
        val s3PresignedPosts: Map<String, Map<String, String>>,
        val s3Endpoint: String
)
