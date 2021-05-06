package com.tempfiledrop.storagesvc.util

import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfo
import org.slf4j.LoggerFactory

object StoragePathUtils {
    private val logger = LoggerFactory.getLogger(StoragePathUtils::class.java)
    /*
     * Process path to ensure that it is in the right format
     * @Params:
     *      path: eg. s3://bucket_name/folder_or_file
     * @Return:
     *      StorageInfo
     */
    fun processStoragePath(bucketName: String, path: String): Pair<Boolean, StorageInfo?> {
        // 1. split bucket and folder path
        val splitPath = path.split("/")

        // 2. Validate target path
        splitPath.forEach {
            if (it.trim().isEmpty()) {
                return Pair(false, null)
            }
        }
        val storagePath = splitPath.joinToString("/")

        return Pair(true, StorageInfo(bucketName, storagePath))
    }
}