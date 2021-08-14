package com.tempstorage.storagesvc.controller.storage

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "Storage S3 Upload Url Response")
data class StorageS3UploadUrlResponse(
        val s3PresignedUrls: Map<String, String>,
        val s3PresignedPosts: Map<String, Map<String, String>>,
        val s3Endpoint: String
)
