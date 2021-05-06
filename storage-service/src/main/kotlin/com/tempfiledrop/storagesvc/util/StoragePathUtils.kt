package com.tempfiledrop.storagesvc.util

import com.tempfiledrop.storagesvc.model.StorageInfo
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
    fun processStoragePath(path: String, numOfFiles: Int): StorageInfo {
        // 1. check that it starts with s3://
        if (path.take(5) != "s3://") {
            return StorageInfo(false)
        }

        // 2. split bucket and folder path
        val splitPath = path.substring(5).split("/")
        val bucketName = splitPath[0]
        val targetSplitPath = splitPath.drop(1)

        // 3. Validate target path
        targetSplitPath.forEach {
            if (it.trim().isEmpty()) {
                return StorageInfo(false)
            }
        }
        val targetFolderPath = targetSplitPath.joinToString("/")

        return StorageInfo(true, bucketName, targetFolderPath, numOfFiles)
    }
}