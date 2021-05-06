package com.tempfiledrop.storagesvc.service.filestorage

import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfo
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path
import java.util.stream.Stream

interface FileStorageService {
    fun initLocalStorage()
    fun saveToFolder(files: List<MultipartFile>, storageInfo: StorageInfo)
    fun loadFromFolder(filename: String): Resource
    fun deleteFilesFromFolder(storageInfoList: List<StorageInfo>)
    fun deleteAllFilesInFolder()
    fun loadAllFilesFromFolder(): Stream<Path>
}