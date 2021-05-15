package com.tempfiledrop.storagesvc.service.storage

import com.tempfiledrop.storagesvc.service.storagefiles.StorageFile
import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfo
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse

interface StorageService {
    fun initStorage()
    fun uploadFiles(files: List<MultipartFile>, storageInfo: StorageInfo): List<StorageFile>
    fun deleteFiles(storageFileList: List<StorageFile>)
    fun downloadFile(storageFile: StorageFile, response: HttpServletResponse)
    fun downloadFilesAsZip(storageFiles: List<StorageFile>, response: HttpServletResponse)
}