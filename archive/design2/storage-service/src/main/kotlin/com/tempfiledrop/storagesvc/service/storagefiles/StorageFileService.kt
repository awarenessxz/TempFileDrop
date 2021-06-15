package com.tempfiledrop.storagesvc.service.storagefiles

interface StorageFileService {
    fun saveFilesInfo(storageId: String, files: List<StorageFile>)
    fun deleteFilesInfo(storageId: String)
    fun deleteFilesInfoBulk(storageIds: List<String>)
    fun getStorageFilesInfoByStorageId(storageId: String): List<StorageFile>
    fun getStorageFilesInfoByStorageIdBulk(storageIds: List<String>): List<StorageFile>
}