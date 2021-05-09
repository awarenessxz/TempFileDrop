package com.tempfiledrop.storagesvc.service.storage

import com.tempfiledrop.storagesvc.service.storageinfo.StorageInfo
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse

interface StorageService {
    fun initStorage()
    fun uploadFiles(files: List<MultipartFile>, storageInfo: StorageInfo)
    fun deleteFiles(storageInfoList: List<StorageInfo>)
    fun downloadFile(storageInfo: StorageInfo, response: HttpServletResponse)
    fun downloadFilesAsZip(storageInfoList: List<StorageInfo>, response: HttpServletResponse)
}