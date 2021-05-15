package com.tempfiledrop.storagesvc.service.cleaner

import com.tempfiledrop.storagesvc.service.storage.StorageService
import com.tempfiledrop.storagesvc.service.storagefiles.StorageFileServiceImpl
import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfoServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CleanupScheduler(
        private val storageInfoService: StorageInfoServiceImpl,
        private val storageFileService: StorageFileServiceImpl,
        private val storageService: StorageService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(CleanupScheduler::class.java)
    }

    // * = match any
    // */X = every X
    // ? = no specific value
    // second, minute, hour, day of month, month, day(s) of week
    @Scheduled(cron = "0 0 0 * * ?")
    fun reportCurrentTime() {
        logger.info("======================================================================")
        logger.info("[Scheduled Job] Cleaning up Storage Information Records....")
        val storageInfoList = storageInfoService.getExpiredStorageInfoList()
        val storageIds = storageInfoList.map { it.id.toString() }
        val storageFiles = storageFileService.getStorageFilesInfoByStorageIdBulk(storageIds)
        logger.info("Deleting ${storageInfoList.size} storageId with a total of ${storageFiles.size} files")
        if (storageFiles.isNotEmpty()) {
            storageService.deleteFiles(storageFiles)
        }
        if (storageIds.isNotEmpty()) {
            storageFileService.deleteFilesInfoBulk(storageIds)
            storageInfoService.deleteStorageInfoByIdBulk(storageIds)
        }
        logger.info("[Scheduled Job] End of Clean up Job")
        logger.info("======================================================================")
    }
}